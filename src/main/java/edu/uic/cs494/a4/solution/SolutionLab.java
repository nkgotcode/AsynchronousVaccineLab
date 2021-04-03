package edu.uic.cs494.a4.solution;

import edu.uic.cs494.a4.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SolutionLab extends Lab<SolutionClinic,SolutionDose> {
    LinkedList<SolutionClinic> clinics = new LinkedList<>();

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
        return result.getResult();
    }

    @Override
    public boolean administerVaccineDoses(SolutionClinic clinic, Set<SolutionDose> vaccineDoses) {
        SolutionResult<Boolean> result = new SolutionResult<>();
        Action<Set<SolutionDose>,Boolean> action = new Action<>(Action.Direction.USE,vaccineDoses,result);
        clinic.submitAction(action);
        return result.getResult();
    }

    @Override
    public boolean discardVaccineDoses(SolutionClinic clinic, Set<SolutionDose> vaccineDoses) {
        SolutionResult<Boolean> result = new SolutionResult<>();
        Action<Set<SolutionDose>,Boolean> action = new Action<>(Action.Direction.DISCARD,vaccineDoses,result);
        clinic.submitAction(action);
        return result.getResult();
    }

    @Override
    public boolean moveVaccineDoses(SolutionClinic from, SolutionClinic to, Set<SolutionDose> vaccineDoses) {
        SolutionResult<Boolean> retFrom = new SolutionResult<>();
        SolutionResult<Boolean> retTo = new SolutionResult<>();
        Action<Set<SolutionDose>,Boolean> asyncFrom = new Action<>(Action.Direction.REMOVE,vaccineDoses,retFrom);
        Action<Set<SolutionDose>,Boolean> asyncTo = new Action<>(Action.Direction.ADD,vaccineDoses,retTo);
        from.submitAction(asyncFrom);
        to.submitAction(asyncTo);

        Result<Boolean> waitResult = new Result<>() {
            @Override
            public void setResult(Boolean result) { throw new Error(); }

            @Override
            public Boolean getResult() {
                Boolean bF = retFrom.getResult();
                Boolean bT = retTo.getResult();
                if (!bF && bT) {
                    Action<Set<SolutionDose>,Boolean> resend = new Action<>(Action.Direction.REMOVE,vaccineDoses,retTo);
                    to.submitAction(resend);
                    Result<Boolean> res = resend.getResult();
                    return false;
                }
                if (bF && !bT) {
                    Action<Set<SolutionDose>,Boolean> resend = new Action<>(Action.Direction.ADD,vaccineDoses,retFrom);
                    from.submitAction(resend);
                    Result<Boolean> res = resend.getResult();
                    return false;
                }
                if (!(bF && bT)) {
                    return false;
                }
                return true;
            }
        };
        return waitResult.getResult();
    }

    @Override
    public Set<SolutionDose> getVaccineDoses() {
        List<SolutionResult<Set<SolutionDose>>> resultList = new LinkedList<>();

        for (var x : clinics) {
            SolutionResult<Set<SolutionDose>> result = new SolutionResult<>();
            Action<SolutionClinic,Set<SolutionDose>> action = new Action<>(Action.Direction.CONTENTS,x,result);
            x.submitAction(action);
            resultList.add(result);
        }
        Result<Set<SolutionDose>> waitResult = new Result<>() {
            @Override
            public void setResult(Set<SolutionDose> result) { throw new Error(); }

            @Override
            public Set<SolutionDose> getResult() {
                Set<SolutionDose> ret = new HashSet<>();
                for (var rs : resultList) {
                    var tmp = rs.getResult();
                    ret.addAll(tmp);
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
        return result.getResult();
    }

    @Override
    public Result<Boolean> addVaccineDosesAsync(SolutionClinic clinic, Set<SolutionDose> vaccineDoses) {
        SolutionResult<Boolean> ret = new SolutionResult<>();
        Action<Set<SolutionDose>,Boolean> asyncAct = new Action<>(Action.Direction.ADD,vaccineDoses,ret);
        clinic.submitAction(asyncAct);
        return ret;
    }

    @Override
    public Result<Boolean> administerVaccineDosesAsync(SolutionClinic clinic, Set<SolutionDose> vaccineDoses) {
        SolutionResult<Boolean> ret = new SolutionResult<>();
        Action<Set<SolutionDose>,Boolean> asyncAct = new Action<>(Action.Direction.USE,vaccineDoses,ret);
        clinic.submitAction(asyncAct);
        return ret;
    }

    @Override
    public Result<Boolean> discardVaccineDosesAsync(SolutionClinic clinic, Set<SolutionDose> vaccineDoses) {
        SolutionResult<Boolean> ret = new SolutionResult<>();
        Action<Set<SolutionDose>,Boolean> asyncAct = new Action<>(Action.Direction.DISCARD,vaccineDoses,ret);
        clinic.submitAction(asyncAct);
        return ret;
    }

    @Override
    public Result<Boolean> moveVaccineDosesAsync(SolutionClinic from, SolutionClinic to, Set<SolutionDose> vaccineDoses) {
        SolutionResult<Boolean> retFrom = new SolutionResult<>();
        SolutionResult<Boolean> retTo = new SolutionResult<>();
        Action<Set<SolutionDose>,Boolean> asyncFrom = new Action<>(Action.Direction.REMOVE,vaccineDoses,retFrom);
        Action<Set<SolutionDose>,Boolean> asyncTo = new Action<>(Action.Direction.ADD,vaccineDoses,retTo);
        from.submitAction(asyncFrom);
        to.submitAction(asyncTo);

        return new Result<>() {
            @Override
            public void setResult(Boolean result) { throw new Error(); }

            @Override
            public Boolean getResult() {
                Boolean bF = retFrom.getResult();
                Boolean bT = retTo.getResult();
                if (!bF && bT) {
                    Action<Set<SolutionDose>,Boolean> resend = new Action<>(Action.Direction.REMOVE,vaccineDoses,retTo);
                    to.submitAction(resend);
                    Result<Boolean> res = resend.getResult();
                    if (!res.getResult()) { // if resend fails

                    }
                    return false;
                }
                if (bF && !bT) {
                    Action<Set<SolutionDose>,Boolean> resend = new Action<>(Action.Direction.ADD,vaccineDoses,retFrom);
                    from.submitAction(resend);
                    return false;
                }
                if (!(bF && bT)) {
                    return false;
                }
                return true;
            }
        };
    }

    @Override
    public Result<Set<SolutionDose>> getVaccineDosesAsync() {
        List<SolutionResult<Set<SolutionDose>>> resultList = new LinkedList<>();

        for (var x : clinics) {
            SolutionResult<Set<SolutionDose>> result = new SolutionResult<>();
            Action<SolutionClinic,Set<SolutionDose>> action = new Action<>(Action.Direction.CONTENTS,x,result);
            x.submitAction(action);
            resultList.add(result);
        }
        return new Result<>() {
            @Override
            public void setResult(Set<SolutionDose> result) { throw new Error(); }

            @Override
            public Set<SolutionDose> getResult() {
                Set<SolutionDose> ret = new HashSet<>();
                for (var rs : resultList) {
                    var tmp = rs.getResult();
                    ret.addAll(tmp);
                }
                return ret;
            }
        };
    }

    @Override
    public Result<Set<SolutionDose>> getVaccineDosesAsync(SolutionClinic clinic) {
        Result<Set<SolutionDose>> ret = new SolutionResult<>();
        Action<SolutionClinic,Set<SolutionDose>> asyncAct = new Action<>(Action.Direction.CONTENTS,clinic,ret);
        clinic.submitAction(asyncAct);
        return ret;
    }
}
