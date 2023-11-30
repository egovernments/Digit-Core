import React from 'react';
import XLSX from 'xlsx';

const GenerateXlsx = ({inputRef}) =>  {
  const handleExport = () => {
    // Sample JSON data
    const jsonData = [
      {
        "code": "WBH_MDMS_MASTER_ACCESSCONTROL_ACTIONS_TEST",
        "message": "Access Control",
        "module": "rainmaker-workbench",
        "locale": Digit.Utils.getDefaultLanguage()
    }
    ];

    // Create a new worksheet
    const ws = XLSX.utils.json_to_sheet(jsonData);

    // Create a new workbook
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1');

    // Save the workbook as an XLSX file
    XLSX.writeFile(wb, 'template.xlsx');
  };

    return (
      <div style={{display:"none"}}>
        <h1>JSON to XLSX Converter</h1>
        <button ref={inputRef} onClick={handleExport}>Export to XLSX</button>
      </div>
    );
}

export default GenerateXlsx;
