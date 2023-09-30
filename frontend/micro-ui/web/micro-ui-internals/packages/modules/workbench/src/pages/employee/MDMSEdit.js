import React,{useState} from 'react'
import MDMSAdd from './MDMSAddV2'
import { Loader,Toast } from '@egovernments/digit-ui-react-components';
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
const MDMSEdit = ({...props}) => {
  const history = useHistory()

  const { t } = useTranslation()

  const { moduleName, masterName, tenantId,uniqueIdentifier } = Digit.Hooks.useQueryParams();
  const stateId = Digit.ULBService.getCurrentTenantId();

  const [showToast, setShowToast] = useState(false);
  const [renderLoader,setRenderLoader] = useState(false)
  const reqCriteria = {
    url: `/${Digit.Hooks.workbench.getMDMSContextPath()}/v2/_search`,
    params: {},
    body: {
      MdmsCriteria: {
        tenantId: stateId,
        uniqueIdentifiers:[uniqueIdentifier],
        schemaCode:`${moduleName}.${masterName}`
      },
    },
    config: {
      enabled: moduleName && masterName && true,
      select: (data) => {
        return data?.mdms?.[0]
      },
    },
  };

  const reqCriteriaSchema = {
    url: `/${Digit.Hooks.workbench.getMDMSContextPath()}/schema/v1/_search`,
    params: {},
    body: {
      SchemaDefCriteria: {
        tenantId: stateId,
        codes:[`${moduleName}.${masterName}`]
      },
    },
    config: {
      enabled: moduleName && masterName && true,
      select: (data) => { 
        const uniqueFields = data?.SchemaDefinitions?.[0]?.definition?.["x-unique"]
        const updatesToUiSchema = {}
        uniqueFields.forEach(field => updatesToUiSchema[field] = {"ui:readonly":true})
        return {schema:data?.SchemaDefinitions?.[0],updatesToUiSchema}
      },
    },
    changeQueryName:"schema"
  };

  const closeToast = () => {
    setTimeout(() => {
      setShowToast(null)
    }, 5000);
  }

  const gotoView = () => { 
    setTimeout(() => {
      setRenderLoader(true)
      history.push(`/${window?.contextPath}/employee/workbench/mdms-view?moduleName=${moduleName}&masterName=${masterName}&uniqueIdentifier=${uniqueIdentifier}`)
    }, 2000);
  }

  const { isLoading, data, isFetching } = Digit.Hooks.useCustomAPIHook(reqCriteria);
  const { isLoading:isLoadingSchema,data: schemaData,isFetching: isFetchingSchema,...rest } = Digit.Hooks.useCustomAPIHook(reqCriteriaSchema);
  

  const reqCriteriaUpdate = {
    url: `/${Digit.Hooks.workbench.getMDMSContextPath()}/v2/_update/${moduleName}.${masterName}`,
    params: {},
    body: {
      
    },
    config: {
      enabled: true,
    },
  };
  const mutation = Digit.Hooks.useCustomAPIMutationHook(reqCriteriaUpdate);

  const handleUpdate = async (formData) => {

    const onSuccess = (resp) => {
      
      setShowToast({
        label:`${t("WBH_SUCCESS_UPD_MDMS_MSG")} ${resp?.mdms?.[0]?.id}`
      });
      // closeToast()
      gotoView()
    };

    const onError = (resp) => {
      setShowToast({
        label:`${t("WBH_ERROR_MDMS_DATA")} ${t(resp?.response?.data?.Errors?.[0]?.code)}`,
        isError:true
      });
      
      closeToast()
    };


    mutation.mutate(
      {
        url:`/${Digit.Hooks.workbench.getMDMSContextPath()}/v2/_update/${moduleName}.${masterName}`,
        params: {},
        body: {
          Mdms:{
            ...data,
            data:formData
          },
        },
      },
      {
        onError,
        onSuccess,
      }
    );

  }

  if(isLoading || isLoadingSchema || renderLoader ) return <Loader />
  
  return (
    <React.Fragment>
      <MDMSAdd defaultFormData = {data?.data} screenType={"edit"} onSubmitEditAction={handleUpdate} updatesToUISchema ={schemaData?.updatesToUiSchema} />
      {showToast && <Toast label={t(showToast.label)} error={showToast?.isError} onClose={()=>setShowToast(null)} ></Toast>}
    </React.Fragment>
  )
}

export default MDMSEdit