let documentFileTypeMappings = {
  docx: "vnd.openxmlformats-officedocument.wordprocessingml.document",
  doc: "application/msword",
  png: "png",
  pdf: "pdf",
  jpeg: "jpeg",
  jpg: "jpeg",
  xls: "vnd.ms-excel",
  xlsx: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  csv: "csv",
};

export const getRegex = (allowedFormats) => {
  // console.log(allowedFormats);
  // if(allowedFormats?.length) {
  //   const obj = { "expression" : `/(.*?)(${allowedFormats?.join('|')})$/`}
  //   const stringified = JSON.stringify(obj);
  //   console.log(new RegExp(JSON.parse(stringified).expression.slice(1, -1)));
  //   return new RegExp(JSON.parse(stringified).expression.slice(1, -1));
  // } else if(docConfig?.allowedFileTypes?.length) {
  //   const obj = { "expression" : `/(.*?)(${docConfig?.allowedFileTypes?.join('|')})$/`}
  //   const stringified = JSON.stringify(obj);
  //   console.log(new RegExp(JSON.parse(stringified).expression.slice(1, -1)))
  //   return new RegExp(JSON.parse(stringified).expression.slice(1, -1));
  // }
  // return /(.*?)(pdf|docx|jpeg|jpg|png|msword|openxmlformats-officedocument|wordprocessingml|document|spreadsheetml|sheet)$/
  if (allowedFormats?.length) {
    let exceptedFileTypes = [];
    allowedFormats?.forEach((allowedFormat) => {
      exceptedFileTypes.push(documentFileTypeMappings[allowedFormat]);
    });
    exceptedFileTypes = exceptedFileTypes.join("|");
    return new RegExp(`(.*?)(${exceptedFileTypes})$`);
  }
  return /(.*?)(pdf|docx|jpeg|jpg|png|msword|openxmlformats-officedocument|wordprocessingml|document|spreadsheetml|sheet)$/;
};

// export { documentFileTypeMappings, getRegex };
