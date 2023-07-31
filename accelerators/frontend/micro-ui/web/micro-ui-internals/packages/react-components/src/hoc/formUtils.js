import { buildYup } from "json-schema-to-yup";



export const buildYupConfig = (jsonConfig,t)=>{
    const errorConfig = {
        // for error messages...
        errMessages: {
        }
      };
      jsonConfig?.required?.map(code=>{
        errorConfig.errMessages[code]={required:`please enter the value for ${code}`}
      })
      Object.keys(jsonConfig?.properties)?.map(code=>{
        if(errorConfig.errMessages[code]){
            errorConfig.errMessages[code]={...errorConfig.errMessages[code],format:`invalid value entered for ${code}`}
        }else{
            errorConfig.errMessages[code]={format:`invalid value entered for ${code}`,"typeError":`invalid type, enter a valid ${code}`}
        }
      })

    const yupSchema = buildYup(jsonConfig, errorConfig);
    return yupSchema

}

