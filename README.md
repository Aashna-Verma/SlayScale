# SlayScale
[![Build and deploy JAR app to Azure Web App - SlayScale](https://github.com/Aashna-Verma/SlayScale/actions/workflows/main_slayscale.yml/badge.svg)](https://github.com/Aashna-Verma/SlayScale/actions/workflows/main_slayscale.yml)
[![Java CI with Maven](https://github.com/Aashna-Verma/SlayScale/actions/workflows/maven.yml/badge.svg)](https://github.com/Aashna-Verma/SlayScale/actions/workflows/maven.yml)
## Overview 
SlayScale is a social product review platform that allows users to collaboratively review and discover. The system encourages trustworthy and social product discovery through community-driven feedback and network analysis. 

Each product is identified by its online listing link and categorized by type. Users can post reviews with star ratings and comments, follow other users whose opinions they trust, and explore insights such as:
- Products ranked by average rating 
- Most followed and most trusted reviewers
- Reviewer similarity based on Jaccard distance
- Degree of separation between users in the follow network

## Current State
- Able to create a new user and add a review by making HTTP requests to the backend
- Implemented `User`, `Product`, and `Review` JPA entities, along with `UserController` and `ProductController`, made for RESTful API endpoints
- CRUD operations for products and reviews, including creating a user and adding reviews for users
- Validation and Error Handling: Unit tests for entities and controllers, input validation for URLs, null checks for entities and exception handling
- Added username uniqueness checks and related validation
- Added RESTful API endpoints for:
    - Following and unfollowing users
    - Retrieving followers and following lists
    - Retrieving and filtering all reviews for a given product and/or created by a given user
    - Retrieving similar reviewers
- Added product search filtering by name and by category
- Added average rating calculation for products
- Added helper utilities for statistical computations
- Implemented Jaccard Distance similarity between users based on shared reviewed products
- Improved error messages and unified response structure
- Added more robust unit tests for new controller paths, validation logic, and edge cases

## Next Sprint Plan
- Implement a sign-in form for existing users to login
- Implement an SPA to support the client-side

## Database Schema
<img width="720" height="685" alt="image" src="https://github.com/user-attachments/assets/1677d0ed-99d8-44f5-b1c2-c9aa26653c6b" />

## UML Diagram