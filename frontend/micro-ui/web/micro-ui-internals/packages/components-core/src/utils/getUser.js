export const getUser = () => {
  try {
    const userType = sessionStorage.getItem("User");
    return userType;
  } catch (error) {
    console.error("Error retrieving User from Digit.SessionStorage:", error);
    return null;
  }
};
