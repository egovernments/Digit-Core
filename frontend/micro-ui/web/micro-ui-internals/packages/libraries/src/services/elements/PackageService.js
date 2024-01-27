import Urls from "../atoms/urls";
import { Request } from "../atoms/Utils/Request";

const PackageService = {
  
  create: (data, tenantId) =>
    Request({
      data: data,
      method: "POST",
      url: Urls.package.create,
      useCache: false,
      params: { tenantId },
      auth: true,
      userService: true,
    }),
};

export default PackageService;
