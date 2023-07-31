import React, { Children } from "react";
import Card from "../Card";

export default {
  title: "Atoms/Card",
  component: Card,
  argTypes: {
    className: {
      control: "text",
    },
    style: {
      control: { type: "object" },
    },
    children: {
      control: "object",
    },
  },
};

const Template = (args) => <Card {...args} />;

export const PlayGround = Template.bind({});
PlayGround.args = {
  pathname: "sanitation-ui/employee/fsm/application-details/107-FSM-2023-06-22-000209",
  style: { border: "1px solid red" },
  children: (
    <p>
      Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Orci a scelerisque
      purus semper. Suspendisse potenti nullam ac tortor vitae. Dictum at tempor commodo ullamcorper a. Amet cursus sit amet dictum sit amet justo.
    </p>
  ),
};

export const Primary = Template.bind({});
Primary.args = {
  pathname: "sanitation-ui/employee/fsm/application-details/107-FSM-2023-06-22-000209",
  children: (
    <p>
      Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Orci a scelerisque
      purus semper. Suspendisse potenti nullam ac tortor vitae. Dictum at tempor commodo ullamcorper a. Amet cursus sit amet dictum sit amet justo.
    </p>
  ),
};
