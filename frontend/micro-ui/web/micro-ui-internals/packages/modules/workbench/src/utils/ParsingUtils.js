// import XLSX from 'xlsx';
import * as XLSX from "xlsx/dist/xlsx.full.min";
export const parseXlsToJson = (event,setter) => {
  event.preventDefault()

  const file = event.target.files[0];
  const reader = new FileReader();
  reader.onload = (e) => {
    const data = new Uint8Array(e.target.result);
    const workbook = XLSX.read(data, { type: 'array' });
    const sheetName = workbook.SheetNames[0]; // Assuming you want the first sheet

    const result =  XLSX.utils.sheet_to_json(workbook.Sheets[sheetName]);   
    setter(()=>result)  
  };

  reader.readAsArrayBuffer(file);
}

export const parseXlsToJsonMultipleSheets  = async (uploadEvent) => {
  const allowedFileTypes = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel'];
  return new Promise((resolve, reject) => {
    const uploadedFile = uploadEvent.target.files[0];

    if (!allowedFileTypes.includes(uploadedFile.type)) {
      reject(new Error('WBH_LOC_INAVLID_FILY_TYPE'));
      return;
    }
    const reader = new FileReader();

    reader.onload = function(event) {
      const arrayBuffer = event.target.result;
      const workbook = XLSX.read(arrayBuffer, { type: 'arraybuffer' });
      const jsonData = {};

      workbook.SheetNames.forEach(sheetName => {
        const worksheet = workbook.Sheets[sheetName];
        const jsonSheetData = XLSX.utils.sheet_to_json(worksheet);
        jsonData[sheetName] = jsonSheetData;
      });

      resolve(jsonData);
    };

    reader.onerror = function(error) {
      reject(error);
    };

    reader.readAsArrayBuffer(uploadEvent.target.files[0]);
  });
}

export const parseXlsToJsonMultipleSheetsFile  = async (uploadedFile) => {
  const allowedFileTypes = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel'];
  return new Promise((resolve, reject) => {

    if (!allowedFileTypes.includes(uploadedFile.type)) {
      reject(new Error('WBH_LOC_INAVLID_FILY_TYPE'));
      return;
    }
    const reader = new FileReader();

    reader.onload = function(event) {
      const arrayBuffer = event.target.result;
      const workbook = XLSX.read(arrayBuffer, { type: 'arraybuffer' });
      const jsonData = {};

      workbook.SheetNames.forEach(sheetName => {
        const worksheet = workbook.Sheets[sheetName];
        const jsonSheetData = XLSX.utils.sheet_to_json(worksheet);
        jsonData[sheetName] = jsonSheetData;
      });

      resolve(jsonData);
    };

    reader.onerror = function(error) {
      reject(error);
    };

    reader.readAsArrayBuffer(uploadedFile);
  });
}