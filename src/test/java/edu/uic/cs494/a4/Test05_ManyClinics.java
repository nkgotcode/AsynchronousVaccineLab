package edu.uic.cs494.a4;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test05_ManyClinics {

    @Test
    public void testCapacityAdd() {
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(1);
        VaccineDose v1 = l.createVaccineDose(0);
        VaccineDose v2 = l.createVaccineDose(1);

        c.startThread();

        {
            l.addVaccineDoses(c, Set.of(v1, v2));

            Set<VaccineDose> expected = Set.of();
            Assert.assertEquals(expected, l.getVaccineDoses());
        }

        {
            Set<VaccineDose> toAdd = new HashSet<>();
            toAdd.add(v1);
            l.addVaccineDoses(c, toAdd);
            toAdd.clear();
            toAdd.add(v2);
            l.addVaccineDoses(c, toAdd);

            Set<VaccineDose> expected = Set.of(v1);
            Assert.assertEquals(expected, l.getVaccineDoses());
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(v1), c.getReadyDoses());
    }

    @Test
    public void testCapacityAddAsync() {
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(1);
        VaccineDose v1 = l.createVaccineDose(0);
        VaccineDose v2 = l.createVaccineDose(1);

        Result<Set<VaccineDose>> r1, r2;

        {
            l.addVaccineDosesAsync(c, Set.of(v1, v2));

            r1 = l.getVaccineDosesAsync();
        }

        {
            l.addVaccineDosesAsync(c, Set.of(v1));
            l.addVaccineDosesAsync(c, Set.of(v2));

            r2 = l.getVaccineDosesAsync();
        }

        c.startThread();

        Assert.assertEquals(Set.of(),   r1.getResult());
        Assert.assertEquals(Set.of(v1), r2.getResult());

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(v1), c.getReadyDoses());
    }

    @Test
    public void testCapacitySameDescription() {
        int size = 10;
        List<Integer> sequentialIndexes = IntStream.range(0, size).boxed().collect(Collectors.toList());
        List<Integer> shuffledIndexesList = IntStream.range(0, size).boxed().collect(Collectors.toList());

        Collections.shuffle(shuffledIndexesList);

        testCapacitySameDescription(sequentialIndexes);
        testCapacitySameDescription(shuffledIndexesList);
    }

    public void testCapacitySameDescription(List<Integer> indexes) {
        int size = indexes.size();
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(size);
        VaccineDose[] vaccineDoses = new VaccineDose[size];
        Set<VaccineDose> toAdd = new HashSet<>();
        Set<VaccineDose> expected = new HashSet<>();

        c.startThread();

        for (int i : indexes) {
            vaccineDoses[i] = l.createVaccineDose(i);
            toAdd.add(vaccineDoses[i]);
            expected.add(vaccineDoses[i]);
        }

        l.addVaccineDoses(c, toAdd);

        Assert.assertEquals(size, l.getVaccineDoses().size());
        Assert.assertEquals(expected, l.getVaccineDoses());
        l.getVaccineDoses(c).clear();
        Assert.assertEquals(size, l.getVaccineDoses().size());
        Assert.assertEquals(expected, l.getVaccineDoses());

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(expected, c.getReadyDoses());
    }


    @Test
    public void testCapacitySameDescriptionAsync() {
        int size = 10;
        List<Integer> sequentialIndexes = IntStream.range(0, size).boxed().collect(Collectors.toList());
        List<Integer> shuffledIndexesList = IntStream.range(0, size).boxed().collect(Collectors.toList());

        Collections.shuffle(shuffledIndexesList);

        testCapacitySameDescriptionAsync(sequentialIndexes);
        testCapacitySameDescriptionAsync(shuffledIndexesList);
    }

    public void testCapacitySameDescriptionAsync(List<Integer> indexes) {
        int size = indexes.size();
        Lab l = Lab.createLab();
        Clinic c = l.createClinic(size);
        VaccineDose[] vaccineDoses = new VaccineDose[size];
        Set<VaccineDose> toAdd = new HashSet<>();
        Set<VaccineDose> expected = new HashSet<>();

        for (int i : indexes) {
            vaccineDoses[i] = l.createVaccineDose(i);
            toAdd.add(vaccineDoses[i]);
            expected.add(vaccineDoses[i]);
        }

        l.addVaccineDosesAsync(c, toAdd);

        c.startThread();

        Result<Set<VaccineDose>> r1 = l.getVaccineDosesAsync();

        Assert.assertEquals(size, r1.getResult().size());
        Assert.assertEquals(expected, r1.getResult());
        l.getVaccineDoses(c).clear();
        Assert.assertEquals(size, l.getVaccineDoses().size());
        Assert.assertEquals(expected, l.getVaccineDoses());

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(expected, c.getReadyDoses());
    }

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
            Assert.assertEquals(expected, l.getVaccineDoses());
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
            Assert.assertEquals(expected, l.getVaccineDoses());

            items.clear();
            items.add(v2);
            l.administerVaccineDoses(c, items);

            expected = Set.of();
            Assert.assertEquals(expected, l.getVaccineDoses());
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

            expected.put(l.getVaccineDosesAsync(), Set.of());
        }

        {
            VaccineDose v1 = l.createVaccineDose(2);
            VaccineDose v2 = l.createVaccineDose(3);

            l.addVaccineDosesAsync(c, Set.of(v1));
            l.addVaccineDosesAsync(c, Set.of(v2));


            l.administerVaccineDosesAsync(c, Set.of(v1));
            expected.put(l.getVaccineDosesAsync(), Set.of(v2));

            l.administerVaccineDosesAsync(c, Set.of(v2));

            expected.put(l.getVaccineDosesAsync(), Set.of());
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
            Assert.assertEquals(expected, l.getVaccineDoses());
        }

        {
            Set<VaccineDose> items = new HashSet<>();
            items.add(v1);
            items.add(v2);

            l.addVaccineDoses(c, items);

            Set<VaccineDose> expected = Set.of(v1, v2);
            Assert.assertEquals(expected, l.getVaccineDoses());

            l.administerVaccineDoses(c, items);
            l.discardVaccineDoses(c, items);

            expected = Set.of();
            Assert.assertEquals(expected, l.getVaccineDoses());

            items.clear();
            items.add(v1);
            l.discardVaccineDoses(c, items);
            Assert.assertEquals(expected, l.getVaccineDoses());

            items.clear();
            items.add(v2);
            l.administerVaccineDoses(c, items);
            Assert.assertEquals(expected, l.getVaccineDoses());
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

            expected.put(l.getVaccineDosesAsync(), Set.of(v1, v2));

            l.administerVaccineDosesAsync(c, Set.of(v1, v2));
            l.discardVaccineDosesAsync(c, Set.of(v1, v2));

//            expected = Set.of();
            expected.put(l.getVaccineDosesAsync(), Set.of());

            l.discardVaccineDosesAsync(c, Set.of(v1));
            expected.put(l.getVaccineDosesAsync(), Set.of());

            l.administerVaccineDosesAsync(c, Set.of(v2));
            expected.put(l.getVaccineDosesAsync(), Set.of());
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
            Assert.assertEquals(expected, l.getVaccineDoses());

            l.getVaccineDoses(c).clear();

            Assert.assertEquals(expected, l.getVaccineDoses());
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

            expected.put(l.getVaccineDosesAsync(), Set.of(v1));

            ((Set<VaccineDose>)l.getVaccineDosesAsync().getResult()).clear();

            expected.put(l.getVaccineDosesAsync(), Set.of(v1));
        }

        for (Map.Entry<Result<Set<VaccineDose>>, Set<VaccineDose>> entry : expected.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(Set.of(v1), c.getReadyDoses());
    }

    @Test
    public void testMaxCapacityClinics() {
        Lab l = Lab.createLab();
        int test = 1_000;
        Clinic[] clinics = new Clinic[test];
        Set<VaccineDose>[] expected = new Set[clinics.length];
        Set<VaccineDose> everything = new HashSet<>();

        for (int i = 0 ; i < clinics.length ; i++) {
            clinics[i] = l.createClinic(i + 1);
            expected[i] = new HashSet<>();
        }

        for (Clinic c : clinics)
            c.startThread();

        int count = 0;


        for (int i = 0 ; i < test ; i++) {
            for (int j = 0 ; i < clinics.length ; i++) {
                Clinic s = clinics[j];
                VaccineDose vaccineDose = l.createVaccineDose(count++);
                Set<VaccineDose> toAdd = new HashSet<>();
                toAdd.add(vaccineDose);

                Assert.assertEquals(everything, l.getVaccineDoses());

                if (i <= j) {
                    Assert.assertTrue(l.addVaccineDoses(s, toAdd));
                    expected[i].add(vaccineDose);
                    everything.add(vaccineDose);
                } else {
                    Assert.assertFalse(l.addVaccineDoses(s, toAdd));
                }
            }
        }


        for (Clinic c : clinics)
            c.stopThread();

        for (int i = 0 ; i < clinics.length ; i++) {
            Clinic c = clinics[i];
            Assert.assertFalse(c.didThrowException());
            Assert.assertEquals(expected[i], c.getReadyDoses());
        }

    }

    @Test
    public void testMaxCapacityClinicsAsync() {
        Lab l = Lab.createLab();
        int test = 1_000;
        Clinic[] clinics = new Clinic[test];
        Set<VaccineDose>[] expected = new Set[clinics.length];
        Set<VaccineDose> everything = new HashSet<>();

        for (int i = 0 ; i < clinics.length ; i++) {
            clinics[i] = l.createClinic(i + 1);
            expected[i] = new HashSet<>();
        }

        int count = 0;

        Map<Result<Boolean>, Boolean> expectedResults = new HashMap<>();
        Map<Result<Set<VaccineDose>>, Set<VaccineDose>> expectedContents = new HashMap<>();

        for (int i = 0 ; i < test ; i++) {
            for (int j = 0 ; i < clinics.length ; i++) {
                Clinic s = clinics[j];
                VaccineDose vaccineDose = l.createVaccineDose(count++);
                Set<VaccineDose> toAdd = new HashSet<>();
                toAdd.add(vaccineDose);

                expectedContents.put(l.getVaccineDosesAsync(), new HashSet<>(everything));

                if (i <= j) {
                    expectedResults.put(l.addVaccineDosesAsync(s, toAdd), true);
                    expected[i].add(vaccineDose);
                    everything.add(vaccineDose);
                } else {
                    expectedResults.put(l.addVaccineDosesAsync(s, toAdd), false);
                }
            }
        }

        for (Clinic c : clinics)
            c.startThread();

        for (Map.Entry<Result<Boolean>, Boolean> entry : expectedResults.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        for (Clinic c : clinics)
            c.stopThread();

        for (Map.Entry<Result<Set<VaccineDose>>, Set<VaccineDose>> entry : expectedContents.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        for (int i = 0 ; i < clinics.length ; i++) {
            Clinic c = clinics[i];
            Assert.assertFalse(c.didThrowException());
            Assert.assertEquals(expected[i], c.getReadyDoses());
        }

    }

    @Test
    public void testRemoveFromLab() {
        int size = 1_000;
        List<Integer> sequentialIndexes = IntStream.range(0, size).boxed().collect(Collectors.toList());
        List<Integer> shuffledIndexesList1 = IntStream.range(0, size).boxed().collect(Collectors.toList());
        List<Integer> shuffledIndexesList2 = IntStream.range(0, size).boxed().collect(Collectors.toList());

        Collections.shuffle(shuffledIndexesList1);
        Collections.shuffle(shuffledIndexesList2);

        testRemoveFromClinic(sequentialIndexes, sequentialIndexes);
        testRemoveFromClinic(shuffledIndexesList1, shuffledIndexesList2);
    }

    public void testRemoveFromClinic(List<Integer> vaccineDoseIndexes, List<Integer> shelfIndexes) {

        Lab l = Lab.createLab();
        int size = vaccineDoseIndexes.size();
        Clinic[] clinics = new Clinic[shelfIndexes.size()];

        for (int i = 0 ; i < clinics.length ; i++)
            clinics[i] = l.createClinic(size);

        VaccineDose[] vaccineDoses = new VaccineDose[size];
        for (int i = 0 ; i < size ; i++)
            vaccineDoses[i] = l.createVaccineDose(i);

        for (Clinic c : clinics)
            c.startThread();

        for (int i : vaccineDoseIndexes) {
            Set<VaccineDose> toAdd = new HashSet<>();
            Clinic s = clinics[shelfIndexes.get(i)];
            Clinic ss = clinics[shelfIndexes.get((i+1)%shelfIndexes.size())];
            toAdd.add(vaccineDoses[i]);
            Assert.assertFalse(l.discardVaccineDoses(s, toAdd));
            Assert.assertFalse(l.administerVaccineDoses(ss, toAdd));

            Assert.assertTrue(l.addVaccineDoses(s, toAdd));

            Assert.assertFalse(l.discardVaccineDoses(ss, toAdd));
            Assert.assertFalse(l.administerVaccineDoses(ss, toAdd));

            if (i%2 == 0)
                Assert.assertTrue(l.administerVaccineDoses(s, toAdd));
            else
                Assert.assertTrue(l.discardVaccineDoses(s, toAdd));
        }

        for (Clinic c : clinics) {
            c.stopThread();

            Assert.assertFalse(c.didThrowException());
            Assert.assertEquals(Set.of(), c.getReadyDoses());
        }
    }

    @Test
    public void testRemoveFromLabAsync() {
        int size = 1_000;
        List<Integer> sequentialIndexes = IntStream.range(0, size).boxed().collect(Collectors.toList());
        List<Integer> shuffledIndexesList1 = IntStream.range(0, size).boxed().collect(Collectors.toList());
        List<Integer> shuffledIndexesList2 = IntStream.range(0, size).boxed().collect(Collectors.toList());

        Collections.shuffle(shuffledIndexesList1);
        Collections.shuffle(shuffledIndexesList2);

        testRemoveFromClinicAsync(sequentialIndexes, sequentialIndexes);
        testRemoveFromClinicAsync(shuffledIndexesList1, shuffledIndexesList2);
    }

    public void testRemoveFromClinicAsync(List<Integer> vaccineDoseIndexes, List<Integer> shelfIndexes) {

        Lab l = Lab.createLab();
        int size = vaccineDoseIndexes.size();
        Clinic[] clinics = new Clinic[shelfIndexes.size()];

        for (int i = 0 ; i < clinics.length ; i++)
            clinics[i] = l.createClinic(size);

        VaccineDose[] vaccineDoses = new VaccineDose[size];
        for (int i = 0 ; i < size ; i++)
            vaccineDoses[i] = l.createVaccineDose(i);

        Map<Result<Boolean>, Boolean> expected = new HashMap<>();

        for (int i : vaccineDoseIndexes) {
            Set<VaccineDose> toAdd = new HashSet<>();
            Clinic s = clinics[shelfIndexes.get(i)];
            Clinic ss = clinics[shelfIndexes.get((i+1)%shelfIndexes.size())];
            toAdd.add(vaccineDoses[i]);
            expected.put(l.discardVaccineDosesAsync(s, toAdd), false);
            expected.put(l.administerVaccineDosesAsync(ss, toAdd), false);

            expected.put(l.addVaccineDosesAsync(s, toAdd), true);

            expected.put(l.discardVaccineDosesAsync(ss, toAdd), false);
            expected.put(l.administerVaccineDosesAsync(ss, toAdd), false);

            if (i%2 == 0)
                expected.put(l.administerVaccineDosesAsync(s, toAdd), true);
            else
                expected.put(l.discardVaccineDosesAsync(s, toAdd), true);
        }

        for (Clinic c : clinics)
            c.startThread();

        for (Map.Entry<Result<Boolean>, Boolean> entry : expected.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        for (Clinic c : clinics) {
            c.stopThread();

            Assert.assertFalse(c.didThrowException());
            Assert.assertEquals(Set.of(), c.getReadyDoses());
        }
    }

    @Test
    public void testRemoveFromEmptyClinics() {
        Lab l = Lab.createLab();
        Clinic c1 = l.createClinic(2);
        Clinic c2 = l.createClinic(2);
        VaccineDose v1 = l.createVaccineDose(1);
        VaccineDose v2 = l.createVaccineDose(2);

        c1.startThread();
        c2.startThread();

        {
            Set<VaccineDose> vaccineDoses = new HashSet<>();
            vaccineDoses.add(v1);
            vaccineDoses.add(v2);

            Assert.assertFalse(l.discardVaccineDoses(c1, vaccineDoses));
            Assert.assertFalse(l.administerVaccineDoses(c1, vaccineDoses));
            Assert.assertFalse(l.discardVaccineDoses(c2, vaccineDoses));
            Assert.assertFalse(l.administerVaccineDoses(c2, vaccineDoses));
        }

        {
            Assert.assertTrue(l.addVaccineDoses(c1, Set.of(v1)));
            Assert.assertTrue(l.addVaccineDoses(c2, Set.of(v2)));
            Assert.assertTrue(l.discardVaccineDoses(c1, Set.of(v1)));
            Assert.assertTrue(l.administerVaccineDoses(c2, Set.of(v2)));

            Set<VaccineDose> vaccineDoses = new HashSet<>();
            vaccineDoses.add(v1);
            vaccineDoses.add(v2);

            Assert.assertFalse(l.discardVaccineDoses(c1, vaccineDoses));
            Assert.assertFalse(l.administerVaccineDoses(c1, vaccineDoses));
            Assert.assertFalse(l.discardVaccineDoses(c2, vaccineDoses));
            Assert.assertFalse(l.administerVaccineDoses(c2, vaccineDoses));

            vaccineDoses.clear();
            vaccineDoses.add(v1);
            Assert.assertFalse(l.discardVaccineDoses(c1, vaccineDoses));
            Assert.assertFalse(l.administerVaccineDoses(c1, vaccineDoses));
            Assert.assertFalse(l.discardVaccineDoses(c2, vaccineDoses));
            Assert.assertFalse(l.administerVaccineDoses(c2, vaccineDoses));

            vaccineDoses.clear();
            vaccineDoses.add(v2);
            Assert.assertFalse(l.discardVaccineDoses(c1, vaccineDoses));
            Assert.assertFalse(l.administerVaccineDoses(c1, vaccineDoses));
            Assert.assertFalse(l.discardVaccineDoses(c2, vaccineDoses));
            Assert.assertFalse(l.administerVaccineDoses(c2, vaccineDoses));
        }

        c1.stopThread();
        c2.stopThread();

        Assert.assertFalse(c1.didThrowException());
        Assert.assertEquals(Set.of(), c1.getReadyDoses());

        Assert.assertFalse(c2.didThrowException());
        Assert.assertEquals(Set.of(), c2.getReadyDoses());
    }

    @Test
    public void testRemoveFromEmptyClinicsAsync() {
        Lab l = Lab.createLab();
        Clinic c1 = l.createClinic(2);
        Clinic c2 = l.createClinic(2);
        VaccineDose v1 = l.createVaccineDose(1);
        VaccineDose v2 = l.createVaccineDose(2);

        Map<Result<Boolean>, Boolean> expected = new HashMap<>();

        {
            Set<VaccineDose> vaccineDoses = new HashSet<>();
            vaccineDoses.add(v1);
            vaccineDoses.add(v2);

            expected.put(l.discardVaccineDosesAsync(c1, vaccineDoses), false);
            expected.put(l.administerVaccineDosesAsync(c1, vaccineDoses), false);
            expected.put(l.discardVaccineDosesAsync(c2, vaccineDoses), false);
            expected.put(l.administerVaccineDosesAsync(c2, vaccineDoses), false);
        }

        {
            expected.put(l.addVaccineDosesAsync(c1, Set.of(v1)), true);
            expected.put(l.addVaccineDosesAsync(c2, Set.of(v2)), true);
            expected.put(l.discardVaccineDosesAsync(c1, Set.of(v1)), true);
            expected.put(l.administerVaccineDosesAsync(c2, Set.of(v2)), true);

            Set<VaccineDose> vaccineDoses = new HashSet<>();
            vaccineDoses.add(v1);
            vaccineDoses.add(v2);

            expected.put(l.discardVaccineDosesAsync(c1, vaccineDoses), false);
            expected.put(l.administerVaccineDosesAsync(c1, vaccineDoses), false);
            expected.put(l.discardVaccineDosesAsync(c2, vaccineDoses), false);
            expected.put(l.administerVaccineDosesAsync(c2, vaccineDoses), false);
        }

        {
            Set<VaccineDose> vaccineDoses = new HashSet<>();
            vaccineDoses.add(v1);
            expected.put(l.discardVaccineDosesAsync(c1, vaccineDoses), false);
            expected.put(l.administerVaccineDosesAsync(c1, vaccineDoses), false);
            expected.put(l.discardVaccineDosesAsync(c2, vaccineDoses), false);
            expected.put(l.administerVaccineDosesAsync(c2, vaccineDoses), false);
        }
        {

            Set<VaccineDose> vaccineDoses = new HashSet<>();
            vaccineDoses.add(v2);
            expected.put(l.discardVaccineDosesAsync(c1, vaccineDoses), false);
            expected.put(l.administerVaccineDosesAsync(c1, vaccineDoses), false);
            expected.put(l.discardVaccineDosesAsync(c2, vaccineDoses), false);
            expected.put(l.administerVaccineDosesAsync(c2, vaccineDoses), false);
        }

        c1.startThread();
        c2.startThread();

        for (Map.Entry<Result<Boolean>, Boolean> entry : expected.entrySet()) {
            Assert.assertEquals(entry.getValue(), entry.getKey().getResult());
        }

        c1.stopThread();
        c2.stopThread();

        Assert.assertFalse(c1.didThrowException());
        Assert.assertEquals(Set.of(), c1.getReadyDoses());

        Assert.assertFalse(c2.didThrowException());
        Assert.assertEquals(Set.of(), c2.getReadyDoses());
    }



}
