import React from 'react'
import MDMSAdd from './MDMSAddV2'
import { Loader } from '@egovernments/digit-ui-react-components';
const MDMSView = ({...props}) => {
  
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
    url: `/mdms-v2/v2/_search/${moduleName}.${masterName}`,
    params: {},
    body: {
      MdmsCriteria: {
        tenantId: stateId,
        uniqueIdentifier
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
    //action===EDIT go to edit screen 
    //action===DISABLE || ENABLE call update api 
  }

  if(isLoading) return <Loader />

  return (
    <MDMSAdd defaultFormData = {data?.data} updatesToUISchema ={{"ui:readonly": true}} screenType={"view"} onViewActionsSelect={onActionSelect} viewActions={fetchActionItems(data)} />
  )
}

export default MDMSView