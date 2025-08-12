import React from "react";
import { LocalSee } from "./LocalSee";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalSee",
  component: LocalSee,
};

export const Default = () => <LocalSee />;
export const Fill = () => <LocalSee fill="blue" />;
export const Size = () => <LocalSee height="50" width="50" />;
export const CustomStyle = () => <LocalSee style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalSee className="custom-class" />;

export const Clickable = () => <LocalSee onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalSee {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
