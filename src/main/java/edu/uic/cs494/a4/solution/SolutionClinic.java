package edu.uic.cs494.a4.solution;

import edu.uic.cs494.a4.Action;
import edu.uic.cs494.a4.Clinic;
import edu.uic.cs494.a4.Result;
import edu.uic.cs494.a4.VaccineDose;

import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.*;

public class SolutionClinic extends Clinic<SolutionDose> {
    final int capacity;
    final LinkedList<Action> actions = new LinkedList<>();

    public SolutionClinic(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public void submitAction(Action a) {
        synchronized (actions) {
            actions.addLast(a);
            actions.notifyAll();
        }
    }

    @Override
    protected Action getAction() {
        while (true) {
            synchronized (actions) {
                if (actions.isEmpty()) {
                    try {
                        actions.wait();
                    } catch (InterruptedException e) {
                        continue;
                    }
                    continue;
                }
                var ret = actions.removeFirst();
                return ret;
            }
        }
    }

    @Override
    protected void add(Set<SolutionDose> doses, Result<Boolean> result) {
        Set<SolutionDose> readyDoses = this.getReadyDoses();
        if (readyDoses.size() + doses.size() > this.capacity) {
            result.setResult(false);
            return;
        }

        for (SolutionDose d : doses) {
            if (readyDoses.contains(d)) {
                result.setResult(false);
                return;
            }
            if (d.status != VaccineDose.Status.READY){
                result.setResult(false);
                return;
            }
        }
        this.addDoses(doses);
        result.setResult(true);
    }
    @Override
    protected void use(Set<SolutionDose> doses, Result<Boolean> result) {
        Set<SolutionDose> readyDoses = this.getReadyDoses();
        if (!readyDoses.containsAll(doses)) {
            result.setResult(false);
            return;
        }

        for (var d : doses) {
            if (d.status == VaccineDose.Status.DISCARDED) {
                result.setResult(false);
                return;
            }
            if (d.getStatus() == VaccineDose.Status.READY) {
                d.status = VaccineDose.Status.USED;
            }
        }
        this.removeDoses(doses);
        result.setResult(true);
    }

    @Override
    protected void discard(Set<SolutionDose> doses, Result<Boolean> result) {
        Set<SolutionDose> readyDoses = this.getReadyDoses();
        if (!readyDoses.containsAll(doses)) {
            result.setResult(false);
            return;
        }

        for (var d : doses) {
            if (d.getStatus() == VaccineDose.Status.READY) {
                d.status = VaccineDose.Status.DISCARDED;
            }
        }
        this.removeDoses(doses);
        result.setResult(true);
    }

    @Override
    protected void contents(Result<Set<SolutionDose>> result) {
        var tmp = this.getReadyDoses();
        result.setResult(tmp);
    }

    @Override
    protected void remove(Set<SolutionDose> doses, Result<Boolean> result) {

    }
}
