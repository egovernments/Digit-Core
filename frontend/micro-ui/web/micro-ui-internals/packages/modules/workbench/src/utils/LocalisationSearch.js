const manageLocParams = ({searchForm,params}) => {
  
  const updatedParams = {
    ...params,
    codes:searchForm?.codes,
    module:searchForm?.module?.value,
    locale:searchForm?.locale?.value
  }
  for (const key in updatedParams) {
    if (updatedParams.hasOwnProperty(key) && !updatedParams[key]) {
      delete updatedParams[key];
    }
  }
  return updatedParams
}

const LocalisationSearchUtil = async ({ url, params, body, plainAccessRequest,state } ) => {
  
  
  const CustomService = Digit.CustomService
  const updatedParams = manageLocParams({...state,params})
  const data = await CustomService.getResponse({ url, params:updatedParams, body, plainAccessRequest })

  const defaultData = await CustomService.getResponse({ url, params:{...updatedParams,locale:"default"}, body, plainAccessRequest })

  const updatedData = Digit?.Customizations?.['commonUiConfig']?.['SearchLocalisationConfig']?.combineData({
    data,
    defaultData,
  })

  return updatedData
}

export const LocalisationSearch = {
  fetchResults : async ({ url, params, body, plainAccessRequest,state }) => {

    try {
      const response = await LocalisationSearchUtil({ url, params, body, plainAccessRequest,state })
      return response
    } catch (error) {
      throw new Error(error?.response?.data?.Errors[0].message);
    }
  }
}