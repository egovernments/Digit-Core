export const getUser = () => {
  try {
    const userType = JSON.parse(sessionStorage.getItem("Digit.User"));
    return userType?.value;
  } catch (error) {
    console.error("Error retrieving User from Digit.SessionStorage:", error);
    return null;
  }
};
