package com.gitanalytics.service;

import com.gitanalytics.entity.CommitRecord;
import com.gitanalytics.repository.CommitRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitCollectorService {

    private final CommitRecordRepository commitRecordRepository;

    @Value("${app.git.clone-dir:/tmp/git-clones}")
    private String cloneDir;

    /**
     * 从本地仓库路径采集提交数据
     *
     * @param repoPath    本地仓库路径
     * @param projectId   项目标识
     * @param since       起始时间
     * @return 采集到的提交记录数
     */
    @Transactional
    public int collectFromLocalPath(String repoPath, String projectId, LocalDateTime since) {
        log.info("开始采集本地仓库: {}, projectId: {}, since: {}", repoPath, projectId, since);
        File repoDir = new File(repoPath);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            log.error("仓库路径不存在或不是目录: {}", repoPath);
            return 0;
        }

        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(new File(repoDir, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();

            long sinceMillis = since.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            List<CommitRecord> records = new ArrayList<>();
            try (Git git = new Git(repository);
                 RevWalk revWalk = new RevWalk(repository)) {

                ObjectId head = repository.resolve("HEAD");
                if (head == null) {
                    log.warn("仓库 {} 没有提交记录", repoPath);
                    return 0;
                }

                revWalk.markStart(revWalk.parseCommit(head));
                revWalk.setRevFilter(org.eclipse.jgit.revwalk.filter.RevFilter.NO_MERGES);

                int count = 0;
                for (RevCommit revCommit : revWalk) {
                    long commitTime = revCommit.getCommitTime() * 1000L;
                    if (commitTime < sinceMillis) {
                        continue;
                    }

                    CommitRecord record = parseCommit(repository, revCommit, projectId);
                    if (record != null) {
                        records.add(record);
                        count++;
                    }

                    // 批量保存，每100条写入一次
                    if (records.size() >= 100) {
                        saveBatch(records);
                        records.clear();
                    }
                }

                if (!records.isEmpty()) {
                    saveBatch(records);
                }

                log.info("仓库 {} 采集完成，共 {} 条提交记录", repoPath, count);
                return count;
            }
        } catch (IOException e) {
            log.error("采集仓库 {} 失败: {}", repoPath, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 解析单个提交记录，统计 diff 行数
     */
    private CommitRecord parseCommit(Repository repository, RevCommit commit, String projectId) {
        try {
            int addLines = 0;
            int deleteLines = 0;
            int fileCount = 0;

            if (commit.getParentCount() > 0) {
                RevCommit parent = commit.getParent(0);
                try (ObjectReader reader = repository.newObjectReader();
                     DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {

                    diffFormatter.setRepository(repository);
                    CanonicalTreeParser parentTree = new CanonicalTreeParser();
                    parentTree.reset(reader, parent.getTree());
                    CanonicalTreeParser commitTree = new CanonicalTreeParser();
                    commitTree.reset(reader, commit.getTree());

                    List<DiffEntry> diffs = diffFormatter.scan(parentTree, commitTree);
                    for (DiffEntry diff : diffs) {
                        fileCount++;
                        // 这里仅统计文件变更数，精确行数统计需要解析 hunk
                        // 使用更简单的方式统计
                    }

                    // 更精确的行数统计
                    for (DiffEntry diff : diffs) {
                        org.eclipse.jgit.diff.EditList edits = diffFormatter.toFileHeader(diff).toEditList();
                        for (org.eclipse.jgit.diff.Edit edit : edits) {
                            addLines += edit.getLengthB();
                            deleteLines += edit.getLengthA();
                        }
                    }
                }
            }

            LocalDateTime commitTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(commit.getCommitTime()), ZoneId.systemDefault());

            String message = commit.getFullMessage();
            boolean automated = detectAutomatedCommit(message, commitTime);

            return CommitRecord.builder()
                    .commitId(commit.getName())
                    .projectId(projectId)
                    .authorEmail(commit.getAuthorIdent().getEmailAddress())
                    .authorName(commit.getAuthorIdent().getName())
                    .commitTime(commitTime)
                    .addLines(addLines)
                    .deleteLines(deleteLines)
                    .netLines(addLines - deleteLines)
                    .fileCount(fileCount)
                    .message(message.substring(0, Math.min(message.length(), 1000)))
                    .mergeCommit(commit.getParentCount() > 1)
                    .automated(automated)
                    .build();
        } catch (IOException e) {
            log.error("解析提交 {} 失败: {}", commit.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * 检测是否为自动化提交（CI/CD、合并冲突修复等）
     */
    private boolean detectAutomatedCommit(String message, LocalDateTime commitTime) {
        String lowerMsg = message.toLowerCase();
        boolean ciPatterns = lowerMsg.contains("merge branch") ||
                lowerMsg.contains("merge pull request") ||
                lowerMsg.contains("chore(release)") ||
                lowerMsg.contains("ci:") ||
                lowerMsg.contains("automation");

        boolean midnightCommit = commitTime.getHour() >= 0 && commitTime.getHour() <= 5;
        return ciPatterns || midnightCommit;
    }

    @Transactional
    void saveBatch(List<CommitRecord> records) {
        for (CommitRecord record : records) {
            if (!commitRecordRepository.existsById(record.getCommitId())) {
                commitRecordRepository.save(record);
            }
        }
    }
}
