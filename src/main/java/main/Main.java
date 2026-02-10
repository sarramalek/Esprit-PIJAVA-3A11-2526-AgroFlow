package test;

import entities.*;
import services.*;
import java.sql.SQLException;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        ServiceAnimal sa = new ServiceAnimal();
        ServiceExamen se = new ServiceExamen();

        try {
            // Test Animal
            animaux v = new animaux(0, "Marguerite", "Bovin", "Holstein", new Date(), Sexe.FEMELLE, 550f);
            sa.ajouter(v);
            System.out.println("Animaux : " + sa.afficher());

            animaux C = new animaux(1,"kiki", "vache", "pure", new Date(), Sexe.MALE, 550f);
            sa.ajouter(v);
            System.out.println("Animaux : " + sa.afficher());

            // Test Examen (Assurez-vous que l'ID animal 1 existe)
            examens ex = new examens(0, new Date(), "Vaccin", "OK", "Repos", 1);
            se.ajouter(ex);
            System.out.println("Examens : " + se.afficher());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}