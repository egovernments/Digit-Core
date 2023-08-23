import { useQuery, useQueryClient } from "react-query";


const useLocalisationSearch = ({ url, params, body, config = {}, plainAccessRequest,changeQueryName="Random" }) => {
  
  const client = useQueryClient();
  const CustomService = Digit.CustomService
  const { isLoading, data, isFetching,refetch } = useQuery(
    [url,changeQueryName].filter((e) => e),
    () => CustomService.getResponse({ url, params, body, plainAccessRequest }),
    {
      cacheTime:0,
      ...config,
    }
  );

  const { isLoading:isLoadingDefault, data:defaultData, isFetching:isFetchingDefault,refetch:refetchDefault } = useQuery(
    [url,changeQueryName,"defaultLocale"].filter((e) => e),
    () => CustomService.getResponse({ url, params:{...params,locale:"default"}, body, plainAccessRequest }),
    {
      cacheTime:0,
      ...config,
    }
  );

  const updatedData = Digit?.Customizations?.['commonUiConfig']?.['SearchLocalisationConfig']?.combineData({
    data,
    defaultData,
    isLoading:isLoadingDefault || isLoading,
    isFetching:isFetchingDefault || isFetching,
    refetch,
    refetchDefault
  })

  return {
    isLoading:isLoadingDefault || isLoading,
    isFetching:isFetchingDefault || isFetching,
    data:updatedData,
    refetch,
    refetchDefault,
    revalidate: () => {
      updatedData && client.invalidateQueries({ queryKey: [url].filter((e) => e) });
    },
  };
};

export default useLocalisationSearch;