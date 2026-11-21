import React from "react";
import { RoundedCheck } from "./RoundedCheck";

export default {
  title: "RoundedCheck",
  component: RoundedCheck,
  argTypes: {
    fill: { control: "color" },
    height: { control: "number" },
    width: { control: "number" },
    style: { control: "object" },
    className: {
      options: ["custom-class"],
      control: { type: "radio" },
    },
    onClick: { action: "clicked" },
  },
};

export const Default = () => <RoundedCheck />;
export const Fill = () => <RoundedCheck fill="blue" />;
export const Size = () => <RoundedCheck height={50} width={50} />;
export const CustomStyle = () => <RoundedCheck style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RoundedCheck className="custom-class" />;

export const Clickable = () => <RoundedCheck onClick={() => console.log("clicked")} />;

const Template = (args) => <RoundedCheck {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  fill: "green",
  height: 30,
  width: 30,
  style: { border: "3px solid green" },
  className: "custom-class",
};
