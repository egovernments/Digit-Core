export const SAMPLE_DID =     {
    "@context": [
        "https://www.w3.org/ns/did/v1"
    ],
    "id": "did:abc:d1e50903-c0ee-42b2-abdf-74f68365759f",
    "alsoKnownAs": [],
    "service": [],
    "verificationMethod": [
        {
            "id": "did:abc:d1e50903-c0ee-42b2-abdf-74f68365759f#key-0",
            "type": "Ed25519VerificationKey2020",
            "@context": "https://w3id.org/security/suites/ed25519-2020/v1",
            "controller": "did:abc:d1e50903-c0ee-42b2-abdf-74f68365759f",
            "publicKeyMultibase": "z6MkwBN1HtfJrBXMtTsSH3HBgUJji3Z8vWTdVqoxshXzcJtP"
        }
    ],
    "authentication": [
        "did:abc:d1e50903-c0ee-42b2-abdf-74f68365759f#key-0"
    ],
    "assertionMethod": [
        "did:abc:d1e50903-c0ee-42b2-abdf-74f68365759f#key-0"
    ]
};

export const SAMPLE_VERIFIABLE_CREDENTIAL = {
    "id": "did:rcw:164f4b00-0141-40ef-b34a-5b9e1d5dfeca",
    "type": [
        "VerifiableCredential",
        "InsuranceCredential"
    ],
    "proof": {
        "type": "Ed25519Signature2020",
        "created": "2024-02-13T09:31:40Z",
        "proofValue": "z3acdb2TypHyxciB5AtB7Y4WJQBUVa3r6aZ7bdF2MNGgb3vuM57nSvY1xAkLJFn4C1bZ26qyprG1mNweyrENUeNCx",
        "proofPurpose": "assertionMethod",
        "verificationMethod": "did:abc:d1e50903-c0ee-42b2-abdf-74f68365759f#key-0"
    },
    "issuer": "did:abc:d1e50903-c0ee-42b2-abdf-74f68365759f",
    "@context": [
        "https://www.w3.org/2018/credentials/v1",
        "https://holashchand.github.io/test_project/insurance-context.json",
        "https://w3id.org/security/suites/ed25519-2020/v1"
    ],
    "issuanceDate": "2024-02-13T09:31:40.464Z",
    "expirationDate": "2033-04-20T20:48:17.684Z",
    "credentialSubject": {
        "id": "did:abc:d1e50903-c0ee-42b2-abdf-74f68365759f",
        "dob": "1968-10-25",
        "type": "InsuranceCredential",
        "email": "shreeram@gmail.com",
        "gender": "Male",
        "mobile": "0123456789",
        "benefits": [
            "Critical Surgery",
            "Full body checkup"
        ],
        "fullName": "Shreeram Theth",
        "policyName": "Start Insurance Gold Premium",
        "policyNumber": "1234567",
        "policyIssuedOn": "2023-04-20T20:48:17.684Z",
        "policyExpiresOn": "2033-04-20T20:48:17.684Z"
    }
}

export const SAMPLE_WEB_DID_VC = {
    "id": "did:rcw:4b9f26f7-a356-4347-bcdb-5dcd6ef52059",
    "type": [
        "VerifiableCredential",
        "InsuranceCredential"
    ],
    "proof": {
        "type": "Ed25519Signature2020",
        "created": "2024-03-26T10:00:56Z",
        "proofValue": "z3FHZma1nUJKeEry1TufZoAtUPMCJ9arDZuGUfuUc4bjogK5PzSajRVqK24nv4KQMP3HWgZsSN4Lhm1b7j6QpLKm6",
        "proofPurpose": "assertionMethod",
        "verificationMethod": "did:web:holashchand.github.io:test_project:32b08ca7-9979-4f42-aacc-1d73f3ac5322#key-0"
    },
    "issuer": "did:web:holashchand.github.io:test_project:32b08ca7-9979-4f42-aacc-1d73f3ac5322",
    "@context": [
        "https://www.w3.org/2018/credentials/v1",
        "https://holashchand.github.io/test_project/insurance-context.json",
        "https://w3id.org/security/suites/ed25519-2020/v1"
    ],
    "issuanceDate": "2024-03-26T10:00:56.039Z",
    "expirationDate": "2024-04-25T10:00:56.024Z",
    "credentialSubject": {
        "id": "did:jwk:eyJrdHkiOiJSU0EiLCJlIjoiQVFBQiIsInVzZSI6InNpZyIsImFsZyI6IlJTMjU2IiwibiI6IndpSFVQVER5cFpnSlE0Uk1FQzFlSDNlNWROTDV5d1hHbC1OeVZQdWM0V0dlazk3YzREcmFGVTJudTZxb0U1V3NCbmhuUUs4ZjVFWTNzYUljQ1RSbHBlLUVsMVp0UGRLbUlGWE1RWWlUV2tlY3N1Mjh2NG1kb013V2NXUlFTRjBRTlBhSUN4dWxzSUl6MHAxVldnUjRfVmdSSlliN0x0bmFtNmlNU1NIVzFacHB3eGZTWjVIOWVLZ0dzbXZvTUlDRDRxU3hBaHpsMlkxc2FpbEZSTTBDTGRUWGJjcVIxdVZGOExjckhIUUwtalVzVlZKNGtTZlRRbHBDVy1ubEJCNzFJT2ljc2lrcVQ5RDI5REN1UjlYRVVZZTB6ck9GMDNVakgxQUpPM0JzUl9ySUt6cnZBNFBrb3ZZcHVmaFdVUU1rcEk4a0h0eE0xcEZaemlCcTNHSmdOMTFZTERJbW5TRVFJNXFydV9ab25wLXVPd0dZdzduWW5xZ0xwdVE3ODMweTZfMkVldzA1T2VRVmNuTTByMGZ6aHctRGR2Q1hmV01GMUdHRnA5Y0Y4U2stR3J6OUt5dFBQVHR5eThCQU0ybEdCY1JodHd0OEFZaEMzT2RNVnNTQUFTbzk1OGdQSkNMNXl4azNaRkJyUHNIVHItMFlReURBMGZ3MTNtVE5id3BNVnBNZWxtVWZ4V2hjWjUtcXNDVXlET0o4c3RnakZZRFlmR1N3WEtOUWg3aTZ2cG0tZ2pIZV9JRXhOeWN1SU9QU1Qtcl9oZ3NBMkJQSWFyTV9YU3RhOGprb3RjTk10azh5U1pLdDJjZ08xcXpzUndCaTRuRmRWRE5mREFna21ZTTBkdS1CcFlqZVZsSXgweC1FcGxXUWpGcURxcDl0UmYtcDVVQ1VfdzgycHNNIn0=",
        "dob": "2000-01-26",
        "email": "tgs@gmail.com",
        "gender": "Male",
        "mobile": "0123456789",
        "benefits": [
            "Critical PSUT",
            "Hepatitas PSUT"
        ],
        "fullName": "TGSStudio",
        "policyName": "Sunbird Insurenc Policy",
        "policyNumber": "55555",
        "policyIssuedOn": "2023-04-20T20:48:17.684Z",
        "policyExpiresOn": "2033-04-20T20:48:17.684Z"
    }
};