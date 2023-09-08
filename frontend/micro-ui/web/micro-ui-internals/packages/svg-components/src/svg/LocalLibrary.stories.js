import React from "react";
import { LocalLibrary } from "./LocalLibrary";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalLibrary",
  component: LocalLibrary,
};

export const Default = () => <LocalLibrary />;
export const Fill = () => <LocalLibrary fill="blue" />;
export const Size = () => <LocalLibrary height="50" width="50" />;
export const CustomStyle = () => <LocalLibrary style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalLibrary className="custom-class" />;

export const Clickable = () => <LocalLibrary onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalLibrary {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
