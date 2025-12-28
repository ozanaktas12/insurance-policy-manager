package com.ozanaktas.insurance.ui;

import com.ozanaktas.insurance.model.Claim;
import com.ozanaktas.insurance.service.ClaimQueueService;
import com.ozanaktas.insurance.service.UndoService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ClaimsQueueView {

    private final ClaimQueueService claimQueueService;
    private final UndoService undoService;
    private final Runnable onBack;

    private final ListView<Claim> queueList = new ListView<>();
    private final ListView<Claim> processedList = new ListView<>();

    private final Label counts = new Label();
    private final Label lastAction = new Label();

    public ClaimsQueueView(ClaimQueueService claimQueueService, UndoService undoService, Runnable onBack) {
        this.claimQueueService = claimQueueService;
        this.undoService = undoService;
        this.onBack = onBack;
    }

    public Parent getView() {
        Label title = new Label("Claims Queue (FIFO) + Undo Stack");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        counts.setStyle("-fx-opacity: 0.85;");
        lastAction.setStyle("-fx-opacity: 0.85;");

        
        Button addDemoClaim = new Button("Add Demo Claim");
        addDemoClaim.setTooltip(new Tooltip("Adds a demo claim for testing.\nReal claims come from Customer → Create Claim."));

        Button processNext = new Button("Process Next (FIFO)");
        Button undo = new Button("Undo Last (LIFO)");
        Button back = new Button("Back");

        addDemoClaim.setMaxWidth(Double.MAX_VALUE);
        processNext.setMaxWidth(Double.MAX_VALUE);
        undo.setMaxWidth(Double.MAX_VALUE);
        back.setMaxWidth(Double.MAX_VALUE);

        addDemoClaim.setOnAction(e -> {
            Claim c = new Claim("customer", "POL-1001", "Demo: Phone screen broken", 2500);
            claimQueueService.submitClaim(c);
            lastAction.setText("Added demo claim.");
            refresh();
        });

        processNext.setOnAction(e -> {
            var processed = claimQueueService.processNext();
            if (processed.isPresent()) {
                lastAction.setText("Processed: " + processed.get().getId().toString().substring(0, 8));
            } else {
                lastAction.setText("No claim in queue.");
            }
            refresh();
        });

        undo.setOnAction(e -> {
            var msg = undoService.undoLast().orElse("Nothing to undo");
            lastAction.setText(msg);
            refresh();
        });

        back.setOnAction(e -> onBack.run());

        HBox buttons = new HBox(10, addDemoClaim, processNext, undo, back);
        HBox.setHgrow(addDemoClaim, Priority.ALWAYS);
        HBox.setHgrow(processNext, Priority.ALWAYS);
        HBox.setHgrow(undo, Priority.ALWAYS);
        HBox.setHgrow(back, Priority.ALWAYS);

        VBox left = new VBox(8, new Label("IN QUEUE"), queueList);
        VBox right = new VBox(8, new Label("PROCESSED"), processedList);
        HBox lists = new HBox(16, left, right);

        left.setPrefWidth(380);
        right.setPrefWidth(380);
        queueList.setPrefHeight(330);
        processedList.setPrefHeight(330);

        VBox root = new VBox(
                10,
                title,
                counts,
                buttons,
                lists,
                new Separator(),
                new Label("Last action:"),
                lastAction
        );
        root.setPadding(new Insets(20));

        refresh();
        return root;
    }

    private void refresh() {
        queueList.getItems().setAll(claimQueueService.getQueueSnapshot());
        processedList.getItems().setAll(claimQueueService.getProcessedSnapshot());

        counts.setText(
                "queued=" + claimQueueService.queuedCount() +
                        " | processed=" + claimQueueService.processedCount() +
                        (undoService.canUndo() ? " | undo=available" : " | undo=empty")
        );
    }
}