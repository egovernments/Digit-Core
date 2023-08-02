import React from 'react'
import MDMSAdd from './MDMSAddV2'
import { Loader } from '@egovernments/digit-ui-react-components';
import { useHistory } from "react-router-dom";

const MDMSView = ({...props}) => {
  const history = useHistory()

  // history.push(`/${window?.contextPath}/employee/expenditure/create-bill`, { contractType /*getContractType()*/  });

  //"ui:readonly": true

  //here make view related things 
  //make data search here itself(once you get it send the appropriate props to mdmsAdd component)

  const { moduleName, masterName, tenantId,uniqueIdentifier } = Digit.Hooks.useQueryParams();
  const stateId = Digit.ULBService.getStateId();

  const fetchActionItems = (data) => {
    let actionItems = [{
      action:"EDIT",
      label:"Edit Master"
    }]

    const isActive = data?.isActive
    if(isActive) actionItems.push({
      action:"DISABLE",
      label:"Disable Master"
    })
    else actionItems.push({
      action:"ENABLE",
      label:"Enable Master"
    })

    return actionItems
  }

  

  const reqCriteria = {
    url: `/mdms-v2/v2/_search`,
    params: {},
    body: {
      MdmsCriteria: {
        tenantId: stateId,
        uniqueIdentifier,
        schemaCodes:[`${moduleName}.${masterName}`]
      },
    },
    config: {
      enabled: moduleName && masterName && true,
      select: (data) => {
        
        return data?.mdms?.[0]
      },
    },
  };

  const { isLoading, data, isFetching } = Digit.Hooks.useCustomAPIHook(reqCriteria);
  
  const onActionSelect = (action) => {
    const {action:actionSelected} = action 
    //action===EDIT go to edit screen 
    if(actionSelected==="EDIT") {
      history.push(`/${window?.contextPath}/employee/workbench/mdms-edit?moduleName=${moduleName}&masterName=${masterName}&uniqueIdentifier=${uniqueIdentifier}`)
    }
    //action===DISABLE || ENABLE call update api 
  }

  if(isLoading) return <Loader />

  return (
    <MDMSAdd defaultFormData = {data?.data} updatesToUISchema ={{"ui:readonly": true}} screenType={"view"} onViewActionsSelect={onActionSelect} viewActions={fetchActionItems(data)} />
  )
}

export default MDMSView