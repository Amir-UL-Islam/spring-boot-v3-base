Bed Table Field:

- Bed Number
- Hospital ID (Foreign Key referencing Hospital Table)
- Is Occupied (Boolean)
- Booked By (Foreign Key referencing Consumer Table, Nullable)
- Status (AVAILABLE, OCCUPIED)
-

Quick Admission:

- Child Name (Full Name) Optional
- Parent Name (Full Name)
- Mobile Number (Will not varify with OTP, just for contact purpose)
- Relationship with the patient (FATHER, MOTHER, SON, DAUGHTER, OTHER)
