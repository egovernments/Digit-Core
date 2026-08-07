import { useQuery, useQueryClient } from "react-query";
import { LocalisationSearch } from "../utils/LocalisationSearch";

const useLocalisationSearch = ({url, params, body, config = {}, plainAccessRequest,changeQueryName="Random",state }) => {
  const client = useQueryClient();
  const CustomService = Digit.CustomService
  const { isLoading, data, isFetching,refetch,error } = useQuery(
    [url,changeQueryName].filter((e) => e),
    () => LocalisationSearch.fetchResults({ url, params, body, plainAccessRequest,state }),
    {
      cacheTime:0,
      ...config,
    }
  );

  return {
    isLoading,
    isFetching,
    data,
    refetch,
    revalidate: () => {
      data && client.invalidateQueries({ queryKey: [url].filter((e) => e) });
    },
    error
  };
};



export default useLocalisationSearch;