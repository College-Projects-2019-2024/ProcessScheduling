package com.collegegroup.processscheduling.GUIControllers;

import com.collegegroup.processscheduling.Comparators.SortByFCFS;
import com.collegegroup.processscheduling.Comparators.SortByPriority_NP;
import com.collegegroup.processscheduling.Comparators.SortBySJF_NP;
import com.collegegroup.processscheduling.GUIProcess;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Hashtable;

import static com.collegegroup.processscheduling.util.*;


public class LiveGanttChartController {
    ObservableList<GUIProcess> processList;

    @FXML HBox hbox;
    private double totalTime = 0, scale;
    private Hashtable<GUIProcess, Double>hash;
    @FXML private TableColumn<GUIProcess, String> pidColumn;
    @FXML private TableColumn<GUIProcess, String> burstColumn;
    @FXML private TableColumn<GUIProcess, String> arrivalTimeColumn;
    @FXML private TableColumn<GUIProcess, String> priorityColumn;

    @FXML private TableColumn<GUIProcess, String> pidColumn2;
    @FXML private TableColumn<GUIProcess, String> startTimeColumn1;
    @FXML private TableColumn<GUIProcess, String> arrivalTimeColumn2;
    @FXML private TableColumn<GUIProcess, String> endTimeColumn1;

    @FXML private TextField pidTextField,burstTextField,arrivalTimeTextField;

    @FXML private TableView<GUIProcess> tableView;
    @FXML private TableView<GUIProcess> tableView2;

    @FXML private Text currentTimeText,avgTurnAroundText,avgWaitingTimeText;
    ArrayList<GUIProcess> processedItems;
    double currentTimeCounter, itr,timeDrawStart;
    Timeline timeline;
    private String mode,quantum = "1";



    @FXML
    public void onClickDraw(ActionEvent ignoredEvent) {
         currentTimeCounter = 0;
         itr = 0;
         timeDrawStart = 0;
         if(mode.equals("FCFS")) FCFS();
         if(mode.equals("SJF")) SJF();
         if(mode.equals("SJF_P")) SJF_P();
         if(mode.equals("Priority")) priority();
         if(mode.equals("Priority_P")) priority_P();
         if(mode.equals("RoundRobin")) roundRobin();
    }


    public void init(ObservableList<GUIProcess> x, double time, String mode)
    {
        //get the process list and the current time
        processList = x;
        totalTime = time;
        scale = time;
        this.mode = mode;
        hbox.setSpacing(0);
        //set the columns of the table to read correct attributes
        pidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        burstColumn.setCellValueFactory(new PropertyValueFactory<>("burst"));
        arrivalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));


        pidColumn2.setCellValueFactory(new PropertyValueFactory<>("pid"));
        arrivalTimeColumn2.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        startTimeColumn1.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn1.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        //fill the table with the list saved from the last scene.
        tableView.setItems(processList);
        //log of the items used for calculations
        processedItems = new ArrayList<>();
        hash = new Hashtable<>();
    }

    public void init(ObservableList<GUIProcess> x, int time, String mode, String quantum)
    {
        //get the process list and the current time
        init(x,time,mode);
        this.quantum = quantum;

    }


    @FXML
    public void insertButtonPushed()
    {
        GUIProcess newProcess = new GUIProcess(pidTextField.getText(),burstTextField.getText(),arrivalTimeTextField.getText());
        if(newProcess.isNotEmpty() && newProcess.isValid()) {
            tableView.getItems().add(newProcess);
            totalTime+=Integer.parseInt(newProcess.getBurst());

        }
    }
    @FXML
    public void deleteButtonPushed() {
        ObservableList<GUIProcess> allProcesses, selected;
        allProcesses = tableView.getItems();
        selected = tableView.getSelectionModel().getSelectedItems();

        for (GUIProcess GUIProcesss : selected) {
            allProcesses.remove(GUIProcesss);
            totalTime-=Integer.parseInt(GUIProcesss.getBurst());
        }

    }



    private void FCFS()
    {
        tableView.getItems().sort(new SortByFCFS());
        timeline = new Timeline(new KeyFrame(Duration.seconds(1),event -> {
            if(tableView.getItems().size()==0)
            {finish(); return;}

            currentTimeText.setText(Double.toString(currentTimeCounter));
            GUIProcess currentProcess = tableView.getItems().get(0);

            if(currentTimeCounter<currentProcess.getArrivalTimeDouble())
            {
                currentTimeCounter++;
                timeDrawStart++;
                return;
            }
            hbox.getChildren().add(liveProcessFactory(currentProcess, currentTimeCounter,1));
            if(currentProcess.getBurstDouble()>0)
            {
                currentProcess.setBurst(currentProcess.getBurstDouble()-1);
                tableView.refresh();
            }

            if(currentProcess.getBurstDouble()==0)
            {   processedItems.add(currentProcess);
                tableView2.getItems().add(currentProcess);
                currentProcess.setEndTime(currentTimeCounter+1);
                tableView.getItems().remove(currentProcess);
                tableView.getItems().sort(new SortByFCFS());
                if(tableView.getItems().size()>0)
                    tableView.getItems().get(0).setStartTime(currentTimeCounter+1);
            }

            currentTimeCounter++;
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                    hbox.getChildren().add(rightEdge(currentTimeCounter));
                    avgTurnAroundText.setText(avgTurnaround());
                    avgWaitingTimeText.setText(avgWaiting());
                    tableView.getItems().clear();
            }
        });
    }
    private void SJF()
    {
        tableView.getItems().sort(new SortBySJF_NP(currentTimeCounter));
        timeline = new Timeline(new KeyFrame(Duration.seconds(1),event -> {
            if(tableView.getItems().size()==0)
            {finish(); return;}
            currentTimeText.setText(Double.toString(currentTimeCounter));
            GUIProcess currentProcess = tableView.getItems().get(0);

            if(currentTimeCounter<currentProcess.getArrivalTimeDouble())
            {
                currentTimeCounter++;
                timeDrawStart++;
                return;
            }
            hbox.getChildren().add(liveProcessFactory(currentProcess, currentTimeCounter,1));
            if(currentProcess.getBurstDouble()>0)
            {
                currentProcess.setBurst(currentProcess.getBurstDouble()-1);
                tableView.refresh();
            }

            if(currentProcess.getBurstDouble()==0)
            {   processedItems.add(currentProcess);
                tableView2.getItems().add(currentProcess);
                currentProcess.setEndTime(currentTimeCounter+1);
                tableView.getItems().remove(currentProcess);
                tableView.getItems().sort(new SortBySJF_NP(currentTimeCounter+1));
                if(tableView.getItems().size()>0)
                    tableView.getItems().get(0).setStartTime(currentTimeCounter+1);
            }
            avgTurnAroundText.setText(avgTurnaround());
            avgWaitingTimeText.setText(avgWaiting());
            currentTimeCounter++;
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                hbox.getChildren().add(rightEdge(currentTimeCounter));
                tableView.getItems().clear();
            }
        });
    }
    private void SJF_P()
    {
        tableView.getItems().sort(new SortBySJF_NP(currentTimeCounter));
         timeline = new Timeline(new KeyFrame(Duration.seconds(1),event -> {
             if(tableView.getItems().size()==0)
             {finish(); return;}
            tableView.getItems().sort(new SortBySJF_NP(currentTimeCounter));
            currentTimeText.setText(Double.toString(currentTimeCounter));
            GUIProcess currentProcess = tableView.getItems().get(0);


            if(currentTimeCounter<currentProcess.getArrivalTimeDouble())
            {
                currentTimeCounter++;
                timeDrawStart++;
                return;
            }
            if(hash.containsKey(currentProcess)){
                if(currentTimeCounter-hash.get(currentProcess) == 0){
                    hash.replace(currentProcess,currentTimeCounter+1);
                }
                else{
                    double p = hash.get(currentProcess);
                    currentProcess.setX(currentProcess.getX()+(currentTimeCounter-p));
                    hash.replace(currentProcess,currentTimeCounter+1);
                }

            }
            else{
                hash.put(currentProcess,currentTimeCounter+1);
            }

            hbox.getChildren().add(liveProcessFactory(currentProcess, currentTimeCounter,1));
            if(currentProcess.getBurstDouble()>0)
            {
                currentProcess.setBurst(currentProcess.getBurstDouble()-1);
                tableView.refresh();
            }

            if(currentProcess.getBurstDouble()==0)
            {   processedItems.add(currentProcess);
                tableView2.getItems().add(currentProcess);
                hash.remove(currentProcess);
                currentProcess.setEndTime(currentTimeCounter+1);
                tableView.getItems().remove(currentProcess);
                tableView.getItems().sort(new SortBySJF_NP(currentTimeCounter+1));
                if(tableView.getItems().size()>0 && !hash.containsKey(tableView.getItems().get(0)))
                    tableView.getItems().get(0).setStartTime(currentTimeCounter+1);
            }



            currentTimeCounter++;
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                hbox.getChildren().add(rightEdge(currentTimeCounter));
                avgWaitingTimeText.setText(avgWaiting());
                avgTurnAroundText.setText(avgTurnaround());
                tableView.getItems().clear();
            }
        });
    }
    private void roundRobin()
    {
        tableView.getItems().sort(new SortByFCFS());
        itr = 0;
        ArrayList<GUIProcess> arrived = new ArrayList<>();

         timeline = new Timeline(new KeyFrame(Duration.seconds(Double.parseDouble(quantum)), event -> {
             currentTimeText.setText(Double.toString(currentTimeCounter));
             tableView.refresh();
             if(tableView.getItems().size()==0)
             {
                 finish();
                 return;
             }

             findArrivedProcesses(currentTimeCounter,arrived);



             var currentProcess = arrived.get((int) itr);
             double min = Math.min(Double.parseDouble(quantum), currentProcess.getBurstDouble());

             timeline.setRate(Math.max(1,Double.parseDouble(quantum)/currentProcess.getBurstDouble()));

             if(hash.containsKey(currentProcess)){
                 if(currentTimeCounter-hash.get(currentProcess) == 0){
                     hash.replace(currentProcess,currentTimeCounter+min);
                 }
                 else{
                     double p = hash.get(currentProcess);
                     currentProcess.setX(currentProcess.getX()+(currentTimeCounter-p));
                     hash.replace(currentProcess,currentTimeCounter+min);
                 }

             }
             else{
                 hash.put(currentProcess,currentTimeCounter+min);
                 currentProcess.setStartTime(currentTimeCounter);

             }
             hbox.getChildren().add(liveProcessFactory(currentProcess,currentTimeCounter, min));

             currentTimeCounter+=Math.min(Integer.parseInt(quantum), currentProcess.getBurstDouble());


             if(currentProcess.getBurstDouble()>0)
             {

                 currentProcess.setBurst(currentProcess.getBurstDouble()-Integer.parseInt(quantum));
             }

             if(currentProcess.getBurstDouble()<=0)
             {
                 processedItems.add(currentProcess);
                 tableView2.getItems().add(currentProcess);
                 hash.remove(currentProcess);
                 currentProcess.setEndTime(currentTimeCounter);
                 tableView.getItems().remove(currentProcess);
                 arrived.remove(currentProcess);

             }
             itr = (itr+1)%arrived.size();
             arrived.sort(new SortByFCFS());


        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                hbox.getChildren().add(rightEdge(currentTimeCounter));
                avgWaitingTimeText.setText(avgWaiting());
                avgTurnAroundText.setText(avgTurnaround());

                tableView.getItems().clear();
            }
        });

    }
    private void priority()
    {
        tableView.getItems().sort(new SortByPriority_NP(currentTimeCounter));
         timeline = new Timeline(new KeyFrame(Duration.seconds(1),event -> {
             if(tableView.getItems().size()==0)
             {finish(); return;}
            currentTimeText.setText(Double.toString(currentTimeCounter));
            GUIProcess currentProcess = tableView.getItems().get(0);

            if(currentTimeCounter<currentProcess.getArrivalTimeDouble())
            {
                currentTimeCounter++;
                timeDrawStart++;
                return;
            }
            hbox.getChildren().add(liveProcessFactory(currentProcess, currentTimeCounter,1));
            if(currentProcess.getBurstDouble()>0)
            {
                currentProcess.setBurst(currentProcess.getBurstDouble()-1);
                tableView.refresh();
            }

            if(currentProcess.getBurstDouble()==0)
            {   processedItems.add(currentProcess);
                tableView2.getItems().add(currentProcess);
                currentProcess.setEndTime(currentTimeCounter+1);
                tableView.getItems().remove(currentProcess);
                tableView.getItems().sort(new SortByPriority_NP(currentTimeCounter+1));
                if(tableView.getItems().size()>0)
                    tableView.getItems().get(0).setStartTime(currentTimeCounter+1);
            }
            avgTurnAroundText.setText(avgTurnaround());
            avgWaitingTimeText.setText(avgWaiting());
            currentTimeCounter++;
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                hbox.getChildren().add(rightEdge(currentTimeCounter));
                tableView.getItems().clear();
            }
        });
    }
    private void priority_P()
    {
        tableView.getItems().sort(new SortByPriority_NP(currentTimeCounter));
         timeline = new Timeline(new KeyFrame(Duration.seconds(1),event -> {
             if(tableView.getItems().size()==0)
             {finish(); return;}
            tableView.getItems().sort(new SortByPriority_NP(currentTimeCounter));
            currentTimeText.setText(Double.toString(currentTimeCounter));
            GUIProcess currentProcess = tableView.getItems().get(0);

            if(currentTimeCounter<currentProcess.getArrivalTimeDouble())
            {
                currentTimeCounter++;
                timeDrawStart++;
                return;
            }
             if(hash.containsKey(currentProcess)){
                 if(currentTimeCounter-hash.get(currentProcess) == 0){
                     hash.replace(currentProcess,currentTimeCounter+1);
                 }
                 else{
                     double p = hash.get(currentProcess);
                     currentProcess.setX(currentProcess.getX()+(currentTimeCounter-p));
                     hash.replace(currentProcess,currentTimeCounter+1);
                 }

             }
             else{
                 hash.put(currentProcess,currentTimeCounter+1);
             }
            hbox.getChildren().add(liveProcessFactory(currentProcess, currentTimeCounter,1));
            if(currentProcess.getBurstDouble()>0)
            {
                currentProcess.setBurst(currentProcess.getBurstDouble()-1);
                tableView.refresh();
            }

            if(currentProcess.getBurstDouble()==0)
            {   processedItems.add(currentProcess);
                tableView2.getItems().add(currentProcess);
                currentProcess.setEndTime(currentTimeCounter+1);
                tableView.getItems().remove(currentProcess);
                tableView.getItems().sort(new SortByPriority_NP(currentTimeCounter+1));
                if(tableView.getItems().size()>0 && !hash.containsKey(tableView.getItems().get(0)))
                    tableView.getItems().get(0).setStartTime(currentTimeCounter+1);
            }

            tableView.getItems().sort(new SortByPriority_NP(currentTimeCounter));
            currentTimeCounter++;
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                hbox.getChildren().add(rightEdge(currentTimeCounter));
                avgWaitingTimeText.setText(avgWaiting());
                avgTurnAroundText.setText(avgTurnaround());
                tableView.getItems().clear();
            }
        });
    }


    private String avgWaiting()
    {
        double sum = 0;
        for(GUIProcess s : processedItems)
        {
            sum += (s.getStartTime()-s.getArrivalTimeDouble()+ s.getX());
        }
        sum /= processedItems.size();
        sum = Math.round(sum*100);
        sum /=100;
        return Double.toString(sum);
    }
    private String avgTurnaround()
    {
        double sum = 0;
        for(GUIProcess s : processedItems)
        {
            sum += (s.getEndTime()-s.getArrivalTimeDouble() );
        }
        sum /= processedItems.size();
        sum = Math.round(sum*100);
        sum /=100;
        return Double.toString(sum);

    }

    private void finish()
    {
        timeline.stop();
//        for(int i = 0; i<hbox.getChildren().size(); i++){
//            if(i == hbox.getChildren().size()-1)break;
//            while(Objects.equals(hbox.getChildren().get(i).getId(), hbox.getChildren().get(i + 1).getId())){
//                hbox.getChildren().get(i).setVisible(false);
//            }
//        }

        timeline.getOnFinished().handle(new ActionEvent());
    }

    private void findArrivedProcesses(double currentTime,ArrayList<GUIProcess> arrived)
    {
        for(var process : tableView.getItems())
        {
            if(process.getArrivalTimeDouble()<=currentTime && !arrived.contains(process))
            {
               arrived.add(process);
            }
        }

    }


}