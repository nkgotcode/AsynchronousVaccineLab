package edu.uic.cs494.a4;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test02_GetSingleClinic {

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
            Assert.assertEquals(expected, l.getVaccineDoses(c));
        }

        {
            Set<VaccineDose> toAdd = new HashSet<>();
            toAdd.add(v1);
            l.addVaccineDoses(c, toAdd);
            toAdd.clear();
            toAdd.add(v2);
            l.addVaccineDoses(c, toAdd);

            Set<VaccineDose> expected = Set.of(v1);
            Assert.assertEquals(expected, l.getVaccineDoses(c));
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

        c.startThread();

        {
            l.addVaccineDosesAsync(c, Set.of(v1, v2));

            Set<VaccineDose> expected = Set.of();
            Result<Set<VaccineDose>> r = l.getVaccineDosesAsync(c);

            Assert.assertEquals(expected, r.getResult());
        }

        {
            Set<VaccineDose> toAdd = new HashSet<>();
            toAdd.add(v1);
            l.addVaccineDosesAsync(c, toAdd).getResult();
            toAdd.clear();
            toAdd.add(v2);
            l.addVaccineDosesAsync(c, toAdd);

            Set<VaccineDose> expected = Set.of(v1);
            Result<Set<VaccineDose>> r = l.getVaccineDosesAsync(c);
            Assert.assertEquals(expected, r.getResult());
        }

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

        Assert.assertEquals(size, l.getVaccineDoses(c).size());
        Assert.assertEquals(expected, l.getVaccineDoses(c));
        l.getVaccineDoses(c).clear();
        Assert.assertEquals(size, l.getVaccineDoses(c).size());
        Assert.assertEquals(expected, l.getVaccineDoses(c));

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

        c.startThread();

        for (int i : indexes) {
            vaccineDoses[i] = l.createVaccineDose(i);
            toAdd.add(vaccineDoses[i]);
            expected.add(vaccineDoses[i]);
        }

        l.addVaccineDosesAsync(c, toAdd);

        Result<Set<VaccineDose>> r1 = l.getVaccineDosesAsync(c);

        Assert.assertEquals(size, r1.getResult().size());
        Assert.assertEquals(expected, r1.getResult());
        l.getVaccineDoses(c).clear();
        Assert.assertEquals(size, l.getVaccineDoses(c).size());
        Assert.assertEquals(expected, l.getVaccineDoses(c));

        c.stopThread();

        Assert.assertFalse(c.didThrowException());
        Assert.assertEquals(expected, c.getReadyDoses());
    }

}
