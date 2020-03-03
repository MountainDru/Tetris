/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.bejinariu.models;

import javafx.concurrent.Task;

/**
 *
 * @author Dru
 */
public class Timer extends Task<Integer> {

    private int timeToSleep;
    private boolean notification = true;

    @Override
    protected Integer call() throws Exception {
        int i = 1;
        while (true) {
            System.out.print("");
            if (isNotification()) {
                Thread.sleep(timeToSleep);
                updateValue(i++);
            }
        }
    }

    public Timer(int timeToSleep) {
        updateValue(1);
        this.timeToSleep = timeToSleep;
    }

    public int getTimeToSleep() {
        return timeToSleep;
    }

    public void setTimeToSleep(int timeToSleep) {
        this.timeToSleep = timeToSleep;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

}
