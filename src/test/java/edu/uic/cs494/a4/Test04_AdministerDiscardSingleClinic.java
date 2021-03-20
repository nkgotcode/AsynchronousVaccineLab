package edu.uic.cs494.a4;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test04_AdministerDiscardSingleClinic {

    @Test
    public void testRemoveFromClinic() {
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(2);

        c.startThread();

        {
            VaccineDose v1 = l.createVaccineDose(0);
            VaccineDose v2 = l.createVaccineDose(1);

            Set<VaccineDose> items = Set.of(v1, v2);
            l.addVaccineDoses(c, items);
            l.discardVaccineDoses(c, items);

            Set<VaccineDose> expected = Set.of();
            Assert.assertEquals(expected, l.getVaccineDoses(c));
        }

        {
            VaccineDose v1 = l.createVaccineDose(2);
            VaccineDose v2 = l.createVaccineDose(3);

            Set<VaccineDose> items = new HashSet<>();
            items.add(v1);
            l.addVaccineDoses(c, items);
            items.clear();
            items.add(v2);
            l.addVaccineDoses(c, items);


            items.clear();
            items.add(v1);
            l.administerVaccineDoses(c, items);

            Set<VaccineDose> expected = Set.of(v2);
            Assert.assertEquals(expected, l.getVaccineDoses(c));

            items.clear();
            items.add(v2);
            l.administerVaccineDoses(c, items);

            expected = Set.of();
            Assert.assertEquals(expected, l.getVaccineDoses(c));
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(), c.getReadyDoses());
    }

    @Test
    public void testRemoveFromClinicAsync() {
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(2);

        Map<Result<Set<VaccineDose>>, Set<VaccineDose>> expected = new HashMap<>();

        {
            VaccineDose v1 = l.createVaccineDose(0);
            VaccineDose v2 = l.createVaccineDose(1);

            Set<VaccineDose> items = Set.of(v1, v2);
            l.addVaccineDosesAsync(c, items);
            l.discardVaccineDosesAsync(c, items);

            expected.put(l.getVaccineDosesAsync(c), Set.of());
        }

        {
            VaccineDose v1 = l.createVaccineDose(2);
            VaccineDose v2 = l.createVaccineDose(3);

            l.addVaccineDosesAsync(c, Set.of(v1));
            l.addVaccineDosesAsync(c, Set.of(v2));


            l.administerVaccineDosesAsync(c, Set.of(v1));
            expected.put(l.getVaccineDosesAsync(c), Set.of(v2));

            l.administerVaccineDosesAsync(c, Set.of(v2));

            expected.put(l.getVaccineDosesAsync(c), Set.of());
        }

        c.startThread();

        for (Map.Entry<Result<Set<VaccineDose>>, Set<VaccineDose>> entry : expected.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(), c.getReadyDoses());
    }


    @Test
    public void testRemoveFromEmptyClinic() {
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(2);
        VaccineDose v1 = l.createVaccineDose(0);
        VaccineDose v2 = l.createVaccineDose(1);

        c.startThread();

        {
            Set<VaccineDose> items = Set.of(v1, v2);

            l.discardVaccineDoses(c, items);

            Set<VaccineDose> expected = Set.of();
            Assert.assertEquals(expected, l.getVaccineDoses(c));
        }

        {
            Set<VaccineDose> items = new HashSet<>();
            items.add(v1);
            items.add(v2);

            l.addVaccineDoses(c, items);

            Set<VaccineDose> expected = Set.of(v1, v2);
            Assert.assertEquals(expected, l.getVaccineDoses(c));

            l.administerVaccineDoses(c, items);
            l.discardVaccineDoses(c, items);

            expected = Set.of();
            Assert.assertEquals(expected, l.getVaccineDoses(c));

            items.clear();
            items.add(v1);
            l.discardVaccineDoses(c, items);
            Assert.assertEquals(expected, l.getVaccineDoses(c));

            items.clear();
            items.add(v2);
            l.administerVaccineDoses(c, items);
            Assert.assertEquals(expected, l.getVaccineDoses(c));
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(), c.getReadyDoses());
    }

    @Test
    public void testRemoveFromEmptyClinicAsync() {
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(2);
        VaccineDose v1 = l.createVaccineDose(0);
        VaccineDose v2 = l.createVaccineDose(1);

        Map<Result<Set<VaccineDose>>, Set<VaccineDose>> expected = new HashMap<>();

        {
            Set<VaccineDose> items = Set.of(v1, v2);

            l.discardVaccineDosesAsync(c, items);

            expected.put(l.getVaccineDosesAsync(c), Set.of());
        }

        {
            l.addVaccineDosesAsync(c, Set.of(v1, v2));

            expected.put(l.getVaccineDosesAsync(c), Set.of(v1, v2));

            l.administerVaccineDosesAsync(c, Set.of(v1, v2));
            l.discardVaccineDosesAsync(c, Set.of(v1, v2));

//            expected = Set.of();
            expected.put(l.getVaccineDosesAsync(c), Set.of());

            l.discardVaccineDosesAsync(c, Set.of(v1));
            expected.put(l.getVaccineDosesAsync(c), Set.of());

            l.administerVaccineDosesAsync(c, Set.of(v2));
            expected.put(l.getVaccineDosesAsync(c), Set.of());
        }

        c.startThread();

        for (Map.Entry<Result<Set<VaccineDose>>, Set<VaccineDose>> entry : expected.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(), c.getReadyDoses());
    }

    @Test
    public void testRemoveVaccineDoseNotInClinic() {
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(1);
        VaccineDose v1 = l.createVaccineDose(0);
        VaccineDose v2 = l.createVaccineDose(1);

        c.startThread();

        {
            Set<VaccineDose> items = new HashSet<>();
            items.add(v1);
            l.addVaccineDoses(c, Set.of(v1));

            items.clear();
            items.add(v2);
            l.administerVaccineDoses(c, items);

            Set<VaccineDose> expected = Set.of(v1);
            Assert.assertEquals(expected, l.getVaccineDoses(c));

            l.getVaccineDoses(c).clear();

            Assert.assertEquals(expected, l.getVaccineDoses(c));
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(v1), c.getReadyDoses());
    }

    @Test
    public void testRemoveVaccineDoseNotInClinicAsync() {
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(1);
        VaccineDose v1 = l.createVaccineDose(0);
        VaccineDose v2 = l.createVaccineDose(1);

        c.startThread();

        Map<Result<Set<VaccineDose>>, Set<VaccineDose>> expected = new HashMap<>();

        {
            Set<VaccineDose> items = new HashSet<>();
            items.add(v1);
            l.addVaccineDoses(c, Set.of(v1));

            items.clear();
            items.add(v2);
            l.administerVaccineDoses(c, items);

            expected.put(l.getVaccineDosesAsync(c), Set.of(v1));

            ((Set<VaccineDose>)l.getVaccineDosesAsync(c).getResult()).clear();

            expected.put(l.getVaccineDosesAsync(c), Set.of(v1));
        }

        for (Map.Entry<Result<Set<VaccineDose>>, Set<VaccineDose>> entry : expected.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(v1), c.getReadyDoses());
    }

    @Test
    public void testVaccineDoseChangesCorrectly() {
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(2);
        VaccineDose v1 = l.createVaccineDose(0);
        VaccineDose v2 = l.createVaccineDose(1);

        c.startThread();

        {
            l.addVaccineDoses(c, Set.of(v1));

            Assert.assertEquals(VaccineDose.Status.READY, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        {
            l.addVaccineDoses(c, Set.of(v2));

            Assert.assertEquals(VaccineDose.Status.READY, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        {
            l.administerVaccineDoses(c, Set.of(v1));

            Assert.assertEquals(VaccineDose.Status.USED, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        {
            l.discardVaccineDoses(c, Set.of(v1));

            Assert.assertEquals(VaccineDose.Status.USED, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        {
            l.discardVaccineDoses(c, Set.of(v2));

            Assert.assertEquals(VaccineDose.Status.USED, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.DISCARDED, v2.getStatus());
        }

        {
            l.administerVaccineDoses(c, Set.of(v2));

            Assert.assertEquals(VaccineDose.Status.USED, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.DISCARDED, v2.getStatus());
        }

        {
            l.addVaccineDoses(c, Set.of(v1));

            Assert.assertEquals(VaccineDose.Status.USED, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.DISCARDED, v2.getStatus());
        }

        {
            l.addVaccineDoses(c, Set.of(v2));

            Assert.assertEquals(VaccineDose.Status.USED, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.DISCARDED, v2.getStatus());
        }

        {
            l.addVaccineDoses(c, Set.of(v1, v2));

            Assert.assertEquals(VaccineDose.Status.USED, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.DISCARDED, v2.getStatus());
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(), c.getReadyDoses());


    }

    @Test
    public void testVaccineDoseChangesCorrectlyAsync() {
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(2);
        VaccineDose v1 = l.createVaccineDose(0);
        VaccineDose v2 = l.createVaccineDose(1);

        Result<Boolean> last;

        {
            l.addVaccineDosesAsync(c, Set.of(v1));

            Assert.assertEquals(VaccineDose.Status.READY, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        {
            l.addVaccineDosesAsync(c, Set.of(v2));

            Assert.assertEquals(VaccineDose.Status.READY, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        {
            l.administerVaccineDosesAsync(c, Set.of(v1));

            Assert.assertEquals(VaccineDose.Status.READY, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        {
            l.discardVaccineDosesAsync(c, Set.of(v1));

            Assert.assertEquals(VaccineDose.Status.READY, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        {
            l.discardVaccineDosesAsync(c, Set.of(v2));

            Assert.assertEquals(VaccineDose.Status.READY, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        {
            l.administerVaccineDosesAsync(c, Set.of(v2));

            Assert.assertEquals(VaccineDose.Status.READY, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        {
            l.addVaccineDosesAsync(c, Set.of(v1));

            Assert.assertEquals(VaccineDose.Status.READY, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        {
            l.addVaccineDosesAsync(c, Set.of(v2));

            Assert.assertEquals(VaccineDose.Status.READY, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        {
            last = l.addVaccineDosesAsync(c, Set.of(v1, v2));

            Assert.assertEquals(VaccineDose.Status.READY, v1.getStatus());
            Assert.assertEquals(VaccineDose.Status.READY, v2.getStatus());
        }

        c.startThread();

        // Once we get the last result, we know that all the previous async operations have completed
        last.getResult();

        c.stopThread();

        Assert.assertEquals(VaccineDose.Status.USED, v1.getStatus());
        Assert.assertEquals(VaccineDose.Status.DISCARDED, v2.getStatus());

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(), c.getReadyDoses());


    }

    @Test
    public void testCannotReuseVaccineDoses() {
        int size = 1_000;
        List<Integer> sequentialIndexes = IntStream.range(0, size).boxed().collect(Collectors.toList());
        List<Integer> shuffledIndexesList = IntStream.range(0, size).boxed().collect(Collectors.toList());

        Collections.shuffle(shuffledIndexesList);

        testCannotReuseVaccineDoses(sequentialIndexes);
        testCannotReuseVaccineDoses(shuffledIndexesList);
    }

    public void testCannotReuseVaccineDoses(List<Integer> indexes) {
        Lab l = Lab.createLab();
        int size = indexes.size();
        Clinic c = l.createClinic(size);

        c.startThread();

        VaccineDose[] vaccineDoses = new VaccineDose[size];
        for (int i = 0 ; i < size ; i++) {
            vaccineDoses[i] = l.createVaccineDose(i);
            Set<VaccineDose> toAdd = new HashSet<>();
            toAdd.add(vaccineDoses[i]);
            Assert.assertTrue(l.addVaccineDoses(c, toAdd));
        }

        for (int i : indexes) {
            Set<VaccineDose> vs = Set.of(vaccineDoses[i]);
            if (i%2 == 0) {
                Assert.assertTrue(l.discardVaccineDoses(c, vs));
            } else {
                Assert.assertTrue(l.administerVaccineDoses(c, vs));
            }

            Assert.assertFalse(l.addVaccineDoses(c, vs));
            Assert.assertFalse(l.discardVaccineDoses(c, vs));
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(), c.getReadyDoses());
    }

    @Test
    public void testCannotReuseVaccineDosesAsync() {
        int size = 1_000;
        List<Integer> sequentialIndexes = IntStream.range(0, size).boxed().collect(Collectors.toList());
        List<Integer> shuffledIndexesList = IntStream.range(0, size).boxed().collect(Collectors.toList());

        Collections.shuffle(shuffledIndexesList);

        testCannotReuseVaccineDosesAsync(sequentialIndexes);
        testCannotReuseVaccineDosesAsync(shuffledIndexesList);
    }

    public void testCannotReuseVaccineDosesAsync(List<Integer> indexes) {
        Lab l = Lab.createLab();
        int size = indexes.size();
        Clinic c = l.createClinic(size);


        Map<Result<Boolean>, Boolean> expected = new HashMap<>();

        VaccineDose[] vaccineDoses = new VaccineDose[size];
        for (int i = 0 ; i < size ; i++) {
            vaccineDoses[i] = l.createVaccineDose(i);
            Set<VaccineDose> toAdd = new HashSet<>();
            toAdd.add(vaccineDoses[i]);
            expected.put(l.addVaccineDosesAsync(c, toAdd), true);
        }

        for (int i : indexes) {
            Set<VaccineDose> vs = Set.of(vaccineDoses[i]);
            if (i%2 == 0) {
                expected.put(l.discardVaccineDosesAsync(c, vs), true);
            } else {
                expected.put(l.administerVaccineDosesAsync(c, vs), true);
            }

            expected.put(l.addVaccineDosesAsync(c, vs), false);
            expected.put(l.discardVaccineDosesAsync(c, vs), false);
        }

        c.startThread();

        for (Map.Entry<Result<Boolean>, Boolean> entry : expected.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(), c.getReadyDoses());
    }

}
