import React from "react";
import { NoResultsFoundIcon } from "./NoResultsFoundIcon";

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "NoResultsFoundIcon",
  component: NoResultsFoundIcon,
};

export const Default = () => <NoResultsFoundIcon />;
export const Fill = () => <NoResultsFoundIcon fill="blue" />;
export const Size = () => <NoResultsFoundIcon height="50" width="50" />;
export const CustomStyle = () => <NoResultsFoundIcon style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NoResultsFoundIcon className="custom-class" />;

export const Clickable = () => <NoResultsFoundIcon onClick={() => console.log("clicked")} />;

const Template = (args) => <NoResultsFoundIcon {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};
