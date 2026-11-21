import React from "react";
import { People } from "./People";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "People",
  component: People,
};

export const Default = () => <People />;
export const Fill = () => <People fill="blue" />;
export const Size = () => <People height="50" width="50" />;
export const CustomStyle = () => <People style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <People className="custom-class" />;

export const Clickable = () => <People onClick={()=>console.log("clicked")} />;

const Template = (args) => <People {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
