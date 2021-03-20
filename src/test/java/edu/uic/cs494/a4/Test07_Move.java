package edu.uic.cs494.a4;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Test07_Move {

    @Test
    public void moveVaccineDoseToEmptyClinic() {
        Lab l = Lab.createLab();
        Clinic c1 = l.createClinic(2);
        Clinic c2 = l.createClinic(2);
        VaccineDose v1 = l.createVaccineDose(1);
        VaccineDose v2 = l.createVaccineDose(2);
        VaccineDose v3 = l.createVaccineDose(3);
        VaccineDose v4 = l.createVaccineDose(4);

        c1.startThread();
        c2.startThread();

        Set<VaccineDose> toAdd = Set.of(v1, v2);
        Assert.assertTrue(l.addVaccineDoses(c1, toAdd));

        Set<VaccineDose> toMove = Set.of(v1, v2);
        Assert.assertTrue(l.moveVaccineDoses(c1, c2, toMove));
        Assert.assertFalse(l.discardVaccineDoses(c1, toMove));
        Assert.assertFalse(l.administerVaccineDoses(c1, toMove));

        Set<VaccineDose> toAddMore = Set.of(v3, v4);
        Assert.assertTrue(l.addVaccineDoses(c1, toAddMore));
        Assert.assertTrue(l.discardVaccineDoses(c2, toMove));

        c1.stopThread();
        c2.stopThread();

        Assert.assertFalse(c1.didThrowException());
        Assert.assertFalse(c2.didThrowException());

        Assert.assertEquals(Set.of(v3, v4), c1.getReadyDoses());
        Assert.assertEquals(Set.of(), c2.getReadyDoses());
    }

    @Test
    public void moveVaccineDoseToEmptyClinicAsync() {
        Lab l = Lab.createLab();
        Clinic c1 = l.createClinic(2);
        Clinic c2 = l.createClinic(2);
        VaccineDose v1 = l.createVaccineDose(1);
        VaccineDose v2 = l.createVaccineDose(2);
        VaccineDose v3 = l.createVaccineDose(3);
        VaccineDose v4 = l.createVaccineDose(4);

        Map<Result<Boolean>, Boolean> expected = new HashMap<>();

        Set<VaccineDose> toAdd = Set.of(v1, v2);
        expected.put(l.addVaccineDosesAsync(c1, toAdd), true);

        Set<VaccineDose> toMove = Set.of(v1, v2);
        expected.put(l.moveVaccineDosesAsync(c1, c2, toMove), true);
        expected.put(l.discardVaccineDosesAsync(c1, toMove), false);
        expected.put(l.administerVaccineDosesAsync(c1, toMove), false);

        Set<VaccineDose> toAddMore = Set.of(v3, v4);
        expected.put(l.addVaccineDosesAsync(c1, toAddMore), true);
        expected.put(l.discardVaccineDosesAsync(c2, toMove), true);

        c1.startThread();
        c2.startThread();

        for (Map.Entry<Result<Boolean>, Boolean> entry : expected.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        c1.stopThread();
        c2.stopThread();

        Assert.assertFalse(c1.didThrowException());
        Assert.assertFalse(c2.didThrowException());

        Assert.assertEquals(Set.of(v3, v4), c1.getReadyDoses());
        Assert.assertEquals(Set.of(), c2.getReadyDoses());
    }

    @Test
    public void moveToFullClinic() {
        Lab l = Lab.createLab();
        Clinic from = l.createClinic(1);
        Clinic to = l.createClinic(1);

        VaccineDose v1 = l.createVaccineDose(1);
        VaccineDose v2 = l.createVaccineDose(2);

        from.startThread();
        to.startThread();

        l.addVaccineDoses(from, Set.of(v1));
        l.addVaccineDoses(to, Set.of(v2));

        {
            Set<VaccineDose> expected = Set.of(v1);
            Assert.assertEquals(expected, l.getVaccineDoses(from));
        }

        {
            Set<VaccineDose> expected = Set.of(v2);
            Assert.assertEquals(expected, l.getVaccineDoses(to));
        }

        Assert.assertFalse(l.moveVaccineDoses(from, to, Set.of(v1)));

        {
            Set<VaccineDose> expected = Set.of(v1);
            Assert.assertEquals(expected, l.getVaccineDoses(from));
        }

        {
            Set<VaccineDose> expected = Set.of(v2);
            Assert.assertEquals(expected, l.getVaccineDoses(to));
        }

        from.stopThread();
        to.stopThread();

        Assert.assertFalse(from.didThrowException());
        Assert.assertFalse(to.didThrowException());

        Assert.assertEquals(Set.of(v1), from.getReadyDoses());
        Assert.assertEquals(Set.of(v2), to.getReadyDoses());
    }

    @Test
    public void moveToFullClinicAsync() {
        Lab l = Lab.createLab();
        Clinic from = l.createClinic(1);
        Clinic to = l.createClinic(1);

        VaccineDose v1 = l.createVaccineDose(1);
        VaccineDose v2 = l.createVaccineDose(2);

        Map<Result<Set<VaccineDose>>, Set<VaccineDose>> expected = new HashMap<>();
        Result<Boolean> moveResult;

        l.addVaccineDosesAsync(from, Set.of(v1));
        l.addVaccineDosesAsync(to, Set.of(v2));

        expected.put(l.getVaccineDosesAsync(from), Set.of(v1));
        expected.put(l.getVaccineDosesAsync(to), Set.of(v2));

        moveResult = l.moveVaccineDosesAsync(from, to, Set.of(v1));

        from.startThread();
        to.startThread();

        Assert.assertFalse(moveResult.getResult());

        expected.put(l.getVaccineDosesAsync(from), Set.of(v1));
        expected.put(l.getVaccineDosesAsync(to), Set.of(v2));
        expected.put(l.getVaccineDosesAsync(), Set.of(v1, v2));

        for (Map.Entry<Result<Set<VaccineDose>>, Set<VaccineDose>> entry : expected.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        from.stopThread();
        to.stopThread();

        Assert.assertFalse(from.didThrowException());
        Assert.assertFalse(to.didThrowException());

        Assert.assertEquals(Set.of(v1), from.getReadyDoses());
        Assert.assertEquals(Set.of(v2), to.getReadyDoses());
    }


    @Test
    public void moveInvalidDose() {
        Lab l = Lab.createLab();
        Clinic from = l.createClinic(1);
        Clinic to = l.createClinic(1);

        VaccineDose v1 = l.createVaccineDose(1);
        VaccineDose v2 = l.createVaccineDose(2);

        from.startThread();
        to.startThread();

        l.addVaccineDoses(from, Set.of(v1));

        {
            Set<VaccineDose> expected = Set.of(v1);
            Assert.assertEquals(expected, l.getVaccineDoses(from));
        }

        {
            Set<VaccineDose> expected = Set.of();
            Assert.assertEquals(expected, l.getVaccineDoses(to));
        }

        Assert.assertFalse(l.moveVaccineDoses(from, to, Set.of(v2)));

        {
            Set<VaccineDose> expected = Set.of(v1);
            Assert.assertEquals(expected, l.getVaccineDoses(from));
        }

        {
            Set<VaccineDose> expected = Set.of();
            Assert.assertEquals(expected, l.getVaccineDoses(to));
        }

        from.stopThread();
        to.stopThread();

        Assert.assertFalse(from.didThrowException());
        Assert.assertFalse(to.didThrowException());

        Assert.assertEquals(Set.of(v1), from.getReadyDoses());
        Assert.assertEquals(Set.of(), to.getReadyDoses());
    }

    @Test
    public void moveInvalidDoseAsync() {
        Lab l = Lab.createLab();
        Clinic from = l.createClinic(1);
        Clinic to = l.createClinic(1);

        VaccineDose v1 = l.createVaccineDose(1);
        VaccineDose v2 = l.createVaccineDose(2);

        Map<Result<Set<VaccineDose>>, Set<VaccineDose>> expected = new HashMap<>();
        Result<Boolean> moveResult;

        l.addVaccineDosesAsync(from, Set.of(v1));

        expected.put(l.getVaccineDosesAsync(from), Set.of(v1));
        expected.put(l.getVaccineDosesAsync(to), Set.of());
//        expected.put(l.getVaccineDosesAsync(), Set.of(v1));

        moveResult = l.moveVaccineDosesAsync(from, to, Set.of(v2));

        from.startThread();
        to.startThread();

        Assert.assertFalse(moveResult.getResult());

        expected.put(l.getVaccineDosesAsync(from), Set.of(v1));
        expected.put(l.getVaccineDosesAsync(to), Set.of());
        expected.put(l.getVaccineDosesAsync(), Set.of(v1));

        for (Map.Entry<Result<Set<VaccineDose>>, Set<VaccineDose>> entry : expected.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        from.stopThread();
        to.stopThread();

        Assert.assertFalse(from.didThrowException());
        Assert.assertFalse(to.didThrowException());

        Assert.assertEquals(Set.of(v1), from.getReadyDoses());
        Assert.assertEquals(Set.of(), to.getReadyDoses());
    }

    @Test
    public void moveInvalidDoseToFullClinic() {
        Lab l = Lab.createLab();
        Clinic from = l.createClinic(1);
        Clinic to = l.createClinic(1);

        VaccineDose v1 = l.createVaccineDose(1);
        VaccineDose v2 = l.createVaccineDose(2);
        VaccineDose v3 = l.createVaccineDose(3);

        from.startThread();
        to.startThread();

        l.addVaccineDoses(from, Set.of(v1));
        l.addVaccineDoses(to, Set.of(v2));

        {
            Set<VaccineDose> expected = Set.of(v1);
            Assert.assertEquals(expected, l.getVaccineDoses(from));
        }

        {
            Set<VaccineDose> expected = Set.of(v2);
            Assert.assertEquals(expected, l.getVaccineDoses(to));
        }

        Assert.assertFalse(l.moveVaccineDoses(from, to, Set.of(v3)));

        {
            Set<VaccineDose> expected = Set.of(v1);
            Assert.assertEquals(expected, l.getVaccineDoses(from));
        }

        {
            Set<VaccineDose> expected = Set.of(v2);
            Assert.assertEquals(expected, l.getVaccineDoses(to));
        }

        from.stopThread();
        to.stopThread();

        Assert.assertFalse(from.didThrowException());
        Assert.assertFalse(to.didThrowException());

        Assert.assertEquals(Set.of(v1), from.getReadyDoses());
        Assert.assertEquals(Set.of(v2), to.getReadyDoses());
    }

    @Test
    public void moveInvalidDoseToFullClinicAsync() {
        Lab l = Lab.createLab();
        Clinic from = l.createClinic(1);
        Clinic to = l.createClinic(1);

        VaccineDose v1 = l.createVaccineDose(1);
        VaccineDose v2 = l.createVaccineDose(2);
        VaccineDose v3 = l.createVaccineDose(3);

        Map<Result<Set<VaccineDose>>, Set<VaccineDose>> expected = new HashMap<>();
        Result<Boolean> moveResult;

        l.addVaccineDosesAsync(from, Set.of(v1));
        l.addVaccineDosesAsync(to, Set.of(v2));

        expected.put(l.getVaccineDosesAsync(from), Set.of(v1));
        expected.put(l.getVaccineDosesAsync(to), Set.of(v2));
        expected.put(l.getVaccineDosesAsync(), Set.of(v1, v2));

        moveResult = l.moveVaccineDosesAsync(from, to, Set.of(v3));

        from.startThread();
        to.startThread();

        Assert.assertFalse(moveResult.getResult());

        expected.put(l.getVaccineDosesAsync(from), Set.of(v1));
        expected.put(l.getVaccineDosesAsync(to), Set.of(v2));
        expected.put(l.getVaccineDosesAsync(), Set.of(v1, v2));

        for (Map.Entry<Result<Set<VaccineDose>>, Set<VaccineDose>> entry : expected.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        from.stopThread();
        to.stopThread();

        Assert.assertFalse(from.didThrowException());
        Assert.assertFalse(to.didThrowException());

        Assert.assertEquals(Set.of(v1), from.getReadyDoses());
        Assert.assertEquals(Set.of(v2), to.getReadyDoses());
    }
}
