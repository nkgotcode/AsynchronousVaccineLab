package edu.uic.cs494.a4.solution;

import edu.uic.cs494.a4.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class SolutionLab extends Lab<SolutionClinic,SolutionDose> {
    LinkedList<SolutionClinic> clinics = new LinkedList<>();
    LinkedList<Action> auditList = new LinkedList<>();

    @Override
    public SolutionClinic createClinic(int capacity) {
        SolutionClinic ret = new SolutionClinic(capacity);
        clinics.addLast(ret);
        return ret;
    }

    @Override
    public SolutionDose createVaccineDose(int id) {
        return new SolutionDose(id);
    }

    @Override
    public boolean addVaccineDoses(SolutionClinic clinic, Set<SolutionDose> vaccineDoses) {
        SolutionResult<Boolean> result = new SolutionResult<>();
        Action<Set<SolutionDose>,Boolean> action = new Action<>(Action.Direction.ADD,vaccineDoses,result);
        clinic.submitAction(action);
        auditList.addLast(action);
        return result.getResult();
    }

    @Override
    public boolean administerVaccineDoses(SolutionClinic clinic, Set<SolutionDose> vaccineDoses) {
        return false;
    }

    @Override
    public boolean discardVaccineDoses(SolutionClinic clinic, Set<SolutionDose> vaccineDoses) {
        return false;
    }

    @Override
    public boolean moveVaccineDoses(SolutionClinic from, SolutionClinic to, Set<SolutionDose> vaccineDoses) {
        return false;
    }

    @Override
    public Set<SolutionDose> getVaccineDoses() {
        List<Result<Set<SolutionDose>>> resultList = new LinkedList<>();
        SolutionResult<Set<SolutionDose>> result = new SolutionResult<>();
        Set<SolutionDose> ret = new HashSet<>();
        for (var x : clinics) {
            Action<SolutionClinic,Set<SolutionDose>> action = new Action<>(Action.Direction.CONTENTS,x,result);
            x.submitAction(action);
            auditList.addLast(action);
        }
        Result<Set<SolutionDose>> waitResult = new Result<>() {
            @Override
            public void setResult(Set<SolutionDose> result) {
                throw new Error();
            }

            @Override
            public Set<SolutionDose> getResult() {
                for (var rs : resultList) {
                    ret.addAll(rs.getResult());
                }
                return ret;
            }
        };
        return waitResult.getResult();
    }

    @Override
    public Set<SolutionDose> getVaccineDoses(SolutionClinic clinic) {
        SolutionResult<Set<SolutionDose>> result = new SolutionResult<>();
        Action<SolutionClinic,Set<SolutionDose>> action = new Action<>(Action.Direction.CONTENTS,clinic,result);
        clinic.submitAction(action);
        auditList.addLast(action);

//        clinic.actions.addLast(action);

        return result.getResult();
    }

    @Override
    public Result<Boolean> addVaccineDosesAsync(SolutionClinic clinic, Set<SolutionDose> vaccineDoses) {
        SolutionResult<Boolean> ret = new SolutionResult<>();
        Action<Set<SolutionDose>,Boolean> asyncAct = new Action<>(Action.Direction.ADD,vaccineDoses,ret);
        auditList.addLast(asyncAct);
        clinic.submitAction(asyncAct);
        auditList.addLast(asyncAct);
        //while (!ret.isReady()) {}
        return ret;
    }

    @Override
    public Result<Boolean> administerVaccineDosesAsync(SolutionClinic clinic, Set<SolutionDose> vaccineDoses) {
        return null;
    }

    @Override
    public Result<Boolean> discardVaccineDosesAsync(SolutionClinic clinic, Set<SolutionDose> vaccineDoses) {
        return null;
    }

    @Override
    public Result<Boolean> moveVaccineDosesAsync(SolutionClinic from, SolutionClinic to, Set<SolutionDose> vaccineDoses) {
        return null;
    }

    @Override
    public Result<Set<SolutionDose>> getVaccineDosesAsync() {
        Result<Set<SolutionDose>> ret = new SolutionResult<>();
        ret.setResult(getVaccineDoses());
        while (!ret.isReady()) {}
        return ret;
    }

    @Override
    public Result<Set<SolutionDose>> getVaccineDosesAsync(SolutionClinic clinic) {
        Result<Set<SolutionDose>> ret = new SolutionResult<>();
        Action<SolutionClinic,Set<SolutionDose>> action = new Action<>(Action.Direction.CONTENTS,clinic,ret);
        clinic.submitAction(action);
//        DoseClinicAction dca = new DoseClinicAction(action,null);
        auditList.addLast(action);
//        ret.setResult(getVaccineDoses(clinic));
//        ret.getResult().
//        while (!ret.isReady()) {}
        return ret;
    }

//    public List<Action> audit (SolutionClinic clinic) {
//        List<Action<SolutionClinic,Result>> ret = new LinkedList<>();
//
//        for (DoseClinicAction dca : auditList) {
//
//        }
//    }

    private static class DoseClinicAction {
        final Action<SolutionClinic,Result> clinicAction;

        final Action<SolutionDose,Result> doseAction;

//        public DoseClinicAction(Action<SolutionClinic,Result> clinicAction) {
//            this.clinicAction = clinicAction;
//        }
//        public DoseClinicAction(Action<SolutionDose,Result> doseAction) {
//            this.doseAction = doseAction;
//        }

        public DoseClinicAction(Action<SolutionClinic,Result> clinicAction, Action<SolutionDose,Result> doseAction) {
            this.clinicAction = clinicAction;
            this.doseAction = doseAction;
        }
    }
}
