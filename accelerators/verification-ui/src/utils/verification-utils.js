import {verifyCredential/*, downloadRevocationList*/} from "verification-sdk";
import {resolveDid} from "./did-utils";

let revocationList = [];

const verify = async (credential) => {
    let resolutionResult = await resolveDid(credential?.proof?.verificationMethod);
    if (resolutionResult.didResolutionMetadata.error) {
        throw new Error(resolutionResult.didResolutionMetadata.error)
    }
    let issuerDID = resolutionResult.didDocument;/*
    let revocationUrl = "http://localhost:3000/credentials/revocation-list";*/
    revocationList = /*await downloadRevocationList(issuerDID.id, revocationUrl)*/[];
    return  await verifyCredential(issuerDID, credential, revocationList);
}

export {verify};
