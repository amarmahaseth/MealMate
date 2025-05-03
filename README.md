# MealMate 🍽️  
MealMate is a smart meal planning and grocery management Android application designed to help users plan their meals, manage grocery lists, delegate shopping tasks, and geotag stores for easier navigation. The app uses Firebase for authentication, real-time database storage, and integrates Google Maps API for geolocation functionality.

---

## 📱 Features

### ✅ Core Features
- **User Authentication**
  - Register with email/password
  - Email verification
  - Login/logout
  - Forgot/reset password
- **Profile Management**
  - View profile details
  - Change password
- **Recipe Management**
  - Create, view, edit, and delete personal recipes
  - Attach images and instructions
- **Grocery List**
  - Auto-generate items from recipes
  - Add, edit, delete custom grocery items
  - Mark items as purchased
  - Share list via SMS
- **Meal Planner**
  - Plan meals (breakfast, lunch, dinner) by assigning recipes to days of the week
  - View today’s plan on the home screen
- **Delegation**
  - Assign shopping tasks with name and phone number
  - Option to select contact from phone
  - Send task via SMS
  - Edit/delete tasks

### 🌟 Optional Feature: Geotag Stores
- Add store locations on Google Maps
- Save and view store names
- Navigate to saved stores using Google Maps
- Delete stores

---

## 🛠️ Tech Stack

- **Language:** Java, XML  
- **IDE:** Android Studio  
- **Backend:** Firebase Realtime Database, Firebase Authentication  
- **Other Services:**  
  - Google Maps API (for geolocation and navigation)  
  - SMS Intent for sharing tasks and lists

---

## 🗂️ Folder Structure

MealMate/
├── app/
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/com/example/mealmate/ # All Java classes
│ │ │ ├── res/ # Layouts, drawables, values
│ │ │ ├── AndroidManifest.xml

