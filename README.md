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
- Core Entities implemented with controllers: `User`, `Product`, and `Review` JPA entities with proper relationships and validations, `UserController` and `ProductController` made for RESTful API endpoints 
- Backend Functionality: CRUD operations for products and reviews, including adding reviews for users
- Validation and Error Handling: Unit tests for entities and controllers, input validation for URLs, null checks for entities and exception handling  

## Next Sprint Plan 
- Add computation and storing of average ratings for products, and update this dynamically when new reviews are added
- Implement Jaccard Distance for similarity between users
- Build a basic, minimal UI for browsing products and posting reviews

## Database Schema
<img width="720" height="685" alt="image" src="https://github.com/user-attachments/assets/1677d0ed-99d8-44f5-b1c2-c9aa26653c6b" />
