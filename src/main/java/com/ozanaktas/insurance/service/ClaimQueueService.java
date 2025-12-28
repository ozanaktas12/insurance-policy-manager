package com.ozanaktas.insurance.service;

import com.ozanaktas.insurance.model.Claim;
import com.ozanaktas.insurance.model.ClaimStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClaimQueueService {

    private final Deque<Claim> queue = new ArrayDeque<>();
    private final List<Claim> processed = new ArrayList<>();
    private final UndoService undoService;

    public ClaimQueueService(UndoService undoService) {
        this.undoService = undoService;
    }

    public void submitClaim(Claim claim) {
        if (claim == null) return;

        if (containsClaimId(claim.getId())) {
            return;
        }

        claim.setStatus(ClaimStatus.IN_QUEUE);
        queue.addLast(claim);
    }

    public Optional<Claim> processNext() {
        Claim next = queue.pollFirst();
        if (next == null) return Optional.empty();

        next.setStatus(ClaimStatus.PROCESSED);
        processed.add(next);

        undoService.push(new UndoableAction() {
            @Override
            public String description() {
                return "Undid processing claim " + next.getId().toString().substring(0, 8) + " (returned to queue)";
            }

            @Override
            public void undo() {
               
                processed.remove(next);
                next.setStatus(ClaimStatus.IN_QUEUE);
                
                if (!queue.contains(next)) {
                    queue.addFirst(next);
                }
            }
        });

        return Optional.of(next);
    }

    private boolean containsClaimId(UUID id) {
        if (id == null) return false;

        for (Claim c : queue) {
            if (id.equals(c.getId())) return true;
        }
        for (Claim c : processed) {
            if (id.equals(c.getId())) return true;
        }
        return false;
    }

    public List<Claim> getQueueSnapshot() {
        return new ArrayList<>(queue);
    }

    public List<Claim> getProcessedSnapshot() {
        return new ArrayList<>(processed);
    }

    public int queuedCount() { return queue.size(); }
    public int processedCount() { return processed.size(); }
}