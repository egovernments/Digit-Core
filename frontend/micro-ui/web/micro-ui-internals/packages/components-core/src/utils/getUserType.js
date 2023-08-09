export const getUserType = () => {
  try {
    const userType = JSON.parse(sessionStorage.getItem("Digit.userType")) || "citizen";
    return userType?.value;
  } catch (error) {
    console.error("Error retrieving userType from Digit.SessionStorage:", error);
    return null;
  }
};
