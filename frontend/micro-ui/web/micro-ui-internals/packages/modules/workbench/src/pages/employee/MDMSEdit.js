import React from 'react'
import MDMSAdd from './MDMSAddV2'
import { Loader } from '@egovernments/digit-ui-react-components';

const MDMSEdit = ({...props}) => {

  const { moduleName, masterName, tenantId,uniqueIdentifier } = Digit.Hooks.useQueryParams();
  const stateId = Digit.ULBService.getStateId();

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

  if(isLoading) return <Loader />

  return (
    <MDMSAdd defaultFormData = {data?.data} screenType={"edit"} />
  )
}

export default MDMSEdit