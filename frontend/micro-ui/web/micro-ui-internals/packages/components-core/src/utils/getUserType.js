export const getUserType = () => {
  try {
    const userType = sessionStorage.getItem("userType");
    return userType;
  } catch (error) {
    console.error("Error retrieving userType from Digit.SessionStorage:", error);
    return null;
  }
};
