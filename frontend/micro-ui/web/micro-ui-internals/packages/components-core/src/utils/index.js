import { useEffect } from "react";

// Get userType from Digit.SessionStorage
export const getUserType = () => {
  try {
    const userType = Digit.SessionStorage.get("userType");
    return userType;
  } catch (error) {
    console.error("Error retrieving userType from Digit.SessionStorage:", error);
    return null;
  }
};

const useOnClickOutside = (ref, handler, isActive, eventParam = false) => {
  useEffect(() => {
    if (isActive) {
      document.addEventListener("click", handleClickOutSide, eventParam);
    } else {
      document.removeEventListener("click", handleClickOutSide, eventParam);
    }
    return () => {
      document.removeEventListener("click", handleClickOutSide, eventParam);
    };
  }, [isActive]);

  const handleClickOutSide = (event) => {
    if (ref.current && ref.current.contains(event.target)) {
      return;
    }
    handler(event);
  };
};

export default useOnClickOutside;
