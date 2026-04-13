Referral Table fields:

- Referral ID (Unique Identifier for each referral)
- Consumer ID (Foreign Key referencing Consumer Table)
- Refer By (Foreign Key referencing Hospital Table)
- Hospital ID Refer To (Foreign Key referencing Hospital Table)
- Hospital ID Refer From (Foreign Key referencing Hospital Table)
- Referral Date (Date and Time of the referral)
- Referral Status (PENDING, CONFIRMED, CANCELLED, COMPLETED)