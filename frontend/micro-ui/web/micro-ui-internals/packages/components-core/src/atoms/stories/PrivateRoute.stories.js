// PrivateRoute.stories.js
import React from "react";
import { Switch, Link, Route } from "react-router-dom";
import { PrivateRoute } from "../PrivateRoute";

// Sample component for protected content
const DummyComponent = () => <div>Protected Content</div>;

export default {
  title: "Atoms/PrivateRoute",
  component: PrivateRoute,
  argTypes: {
    roles: {
      control: "array",
    },
  },
};

const Template = (args) => (
  <Switch>
    <Route path="/" element={<Link to="/protected">Go to Protected Content</Link>} />
    <PrivateRoute {...args} path="/protected" component={() => <DummyComponent />} />
  </Switch>
);

export const WithoutLogin = Template.bind({});
WithoutLogin.args = {
  roles: ["admin", "employee", "citizen"],
};

export const WithLogin = Template.bind({});
WithLogin.args = {
  roles: ["admin"],
};
