## This project was developed collaboratively by a team of students as part of the "Produkto vystymo projektas" module at Kaunas University of Technology."

## Contributors
### Backend
- [Vitalijus Pamakstys](https://github.com/Pamakstys) - responsable for authorization, user addresses and data update, emails/reminders, requests, stripe payment, schedules
- [Teo](https://github.com/Teo-03) - responsable for malfunctions, reviews


A web system designed for both **users** and **employees** to manage services such as rubbish schedules, malfunctions, requests, and payments.

Built with **Java Spring Boot**, **MySQL**, and **React**.

---

## ✨ Features

### 🔑 Authentication
- JWT-based authentication for secure user and employee access.

---

### 👤 User Features
- Add house/property address
- Dynamically fetch rubbish/trash schedule from [grafikai.svara.lt](https://grafikai.svara.lt/)
- View and download schedules as `.ics` calendar files
- Get reminders (1 day before) about rubbish takeout
- Submit malfunctions (e.g. electricity, water issues)
- Submit service requests from employee-provided templates (e.g. change trash collection frequency, order a rubbish container)
- Pay multiple bills at once using **Stripe**
- Leave a review about the site
- Update personal data

---

### 🛠️ Employee Features
- Dynamically create service request templates
- Attach `.docx` templates with placeholders for automatic document generation
- Review & update user-submitted malfunctions (auto-assigned by the system)
- Review & process user requests (auto-assigned by the system)
- Receive email notifications for new requests/malfunctions
- Get daily reminders for incomplete tasks at the start of the workday

---

## ⚙️ Technologies

- **Backend:** Java Spring Boot
- **Database:** MySQL
- **Frontend:** React (currently not public)
- **Payments:** Stripe
