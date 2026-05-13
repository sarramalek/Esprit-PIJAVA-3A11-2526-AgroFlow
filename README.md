# AgroFlow


User Module – Role-based access control (Super Admin, Agriculture, Employee), Two-Factor Authentication (2FA) for enhanced security, Speech-to-Text & Text-to-Speech accessibility features, employee repartition by terrain, and a comprehensive user management dashboard

Animal Management – CRUD operations for livestock, health status tracking, automated PDF health record generation, and detailed animal profiles

Terrain Management – Field/land parcel registration, terrain allocation and monitoring with recent improvements

Health Record Module – Generate comprehensive health booklets (Carnet de Santé) in PDF format with data from multiple related tables

Subscription & Offers Management – Farmers can subscribe to one or more subscription plans simultaneously

Alert System – Real-time notifications for critical events and updates

Reporting – Data analysis and activity reports for decision support

Tech Stack

Frontend
JavaFX / Thymeleaf – Desktop client / web views
CSS / Bootstrap – Styling and responsive layout
JavaScript – Client-side interactivity

Backend

Java 17+ – Core programming language
Spring Boot – Application framework and REST API
Spring Data JPA / Hibernate – ORM and database access
Spring Security – Authentication, authorization & 2FA
MySQL – Relational database
Maven – Build automation and dependency management
JUnit – Unit testing framework

Architecture 

AgroFlow follows the MVC (Model-View-Controller) . The application is organized into domain-specific modules with role-based security enforced through Symfony's security component.

Contributors 
Sarra Malek User Module (2FA, Accessibility, Employee-Terrain Repartition, Subscription & Offers, Alert System) 
Nourane Landoulsi Terrain & Rotation Module (CRUD, AI Algorithms, External APIs, AgroBot, PDF Export, Maps, Weather, Air Quality) yessmne rezgui Partner & Animal Care Module (CRUD, Geolocation + Gemini AI, Nearest Vet Finder, Open Food Facts, Statistics, Vaccination Alerts, TTS, Translation, Encyclopedia, Pagination, Breeding Algorithm)
Eya Mallouli Stocks & articles 
Yessmine Rezgui Animals & checkups 
Eya laadjimi Materiels & Maitenance

Academic Context

Developed at Esprit School of Engineering – Tunisia PIDEV – 3A | 2025–2026

Avknowledgement 

We would like to express our sincere gratitude to:

Esprit School of Engineering for providing a comprehensive academic framework and the opportunity to work on this integrated project
Our project supervisors and technical tutors for their valuable guidance, continuous support, and constructive feedback throughout the development process
Our team members for their collaboration, dedication, and shared commitment to building a meaningful agricultural solution
The open-source community for the tools and frameworks that made this project possible
This project represents the culmination of our learning journey in the 3rd year of the engineering program, and we are grateful for every challenge that helped us grow.