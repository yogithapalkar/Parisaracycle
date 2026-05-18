# Parisaracycle: Campus Lost & Found Platform

Parisaracycle is a centralized, community-driven native mobile application designed to solve the problem of fragmented communication and low recovery rates for misplaced items within a college campus ecosystem. 

By transitioning away from unorganized spreadsheets, chaotic instant messaging threads, and cluttered social media groups, Parisaracycle provides a single, trusted source of truth for students, faculty, and visitors to report, track, and recover lost belongings.

## 🚀 Key Features

- **Unified Reporting Dashboard:** Streamlined workflows divided into clear "I Lost Something" and "I Found Something" reporting channels.
- **AI-Powered Semantic Matchmaking:** Leverages natural language processing to automatically correlate items based on unstructured textual descriptions (e.g., matching a lost report for a "dark metal flask" with a found entry for a "black water bottle").
- **Real-Time Search & Advanced Filtering:** Instantly narrow down the campus inventory by category, location tags, date ranges, or specific colors.
- **Secure Claim Verification Flow:** A structured process managed by campus desk personnel requiring descriptive proof or matching verification points before an item status is updated to successfully returned.

## 🛠️ Technology Stack & Development Strategy

The engineering workflow utilizes a distinct two-phase implementation paradigm:

1. **Prototyping Layer (Google AI Studio):**
   - Used to architect, test, and refine the semantic matching rules.
   - Fine-tunes prompt engineering pipelines to ensure highly accurate cross-referencing between user descriptions without reliance on rigid, hardcoded keyword matching algorithms.
   
2. **Production Layer (Android Studio):**
   - Developed as a native mobile client using **Kotlin** and **Jetpack Compose** to maximize UI responsiveness and local hardware efficiency.
   - Securely communicates with the fine-tuned intelligence layer via the integrated **Google AI Client SDK**.

## 📱 Target Audience

- **Campus Students & Faculty:** Universal end-users seeking an efficient interface to quickly list missing articles or log items discovered across academic blocks.
- **Campus Visitors & Guests:** Accessible to the broader community without requiring internal corporate intranet credentials to facilitate open discovery.
- **Campus Security & Helpdesk Administrators:** Custodians overseeing the physical inventory lockers, updating logging verification statuses, and authenticating claimant ownership.

## 🔮 Future Enhancements

- **Computer Vision Integration:** Incorporating automated image recognition to tag object details (e.g., brand, item type, color) instantly upon uploading a photograph.
- **Localized Push Notifications:** Triggering immediate geo-fenced alerts to nearby users when an item is marked lost in specific campus zones (e.g., cafeteria, sports complex, library).
- **Secure QR Code Tagging:** Introducing optional scannable QR codes for high-value student items (laptops, identity badges, wallets) to make reporting found belongings an instantaneous one-click action.
