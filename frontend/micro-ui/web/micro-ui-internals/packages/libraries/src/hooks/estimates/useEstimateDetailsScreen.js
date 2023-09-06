import { WorksSearch } from "../../services/molecules/Works/Search";
import { useQuery } from "react-query";

const useEstimateDetailsScreen = (t, tenantId, estimateNumber, config,isStateChanged ) => {
    
    return useQuery(
        ["ESTIMATE_WORKS_SEARCH", "ESTIMATE_SEARCH", tenantId, estimateNumber, isStateChanged],
        () => WorksSearch.viewEstimateScreen(t, tenantId, estimateNumber),
        {staleTime:0,...config}
    );
}

export default useEstimateDetailsScreen