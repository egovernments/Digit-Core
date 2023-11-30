import Urls from "../atoms/urls";
import { Request } from "../atoms/Utils/Request";

const PackageService = {
  
  create: (data, tenantId) =>
    Request({
      method: "POST",
      url: Urls.package.create,
      data: data,
      useCache: false,
      params: { tenantId },
      auth: true,
      userService: true,
    }),
};

export default PackageService;
