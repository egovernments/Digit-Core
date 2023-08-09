import { useEffect, useState } from "react";

const useRouteSubscription = (pathname) => {
  const [classname, setClassname] = useState("citizen");
  useEffect(() => {
    const employeePages = ["search", "inbox", "dso-dashboard", "dso-application-details", "user"];
    const isEmployeeUrl = employeePages.some((url) => pathname?.split("/")?.includes(url));
    if (isEmployeeUrl && classname === "citizen") {
      setClassname("employee");
    } else if (!isEmployeeUrl && classname === "employee") {
      setClassname("citizen");
    }
  }, [pathname]);

  return classname;
};

export default useRouteSubscription;
