import {AlertInfo, QrScanResult} from "./data-types";

export type SetActiveStepFunction = (activeStep: number) => void;
export type SetVcStatusFunction = (vc: any) => void;
export type SetQrDataFunction = (qrData: string) => void;
export type SetScanResultFunction = (result: QrScanResult) => void;
export type SetAlertInfoFunction = ((alert: AlertInfo) => void) | ((setUsingCurrentAlert: ((alert: AlertInfo) => AlertInfo)) => void);