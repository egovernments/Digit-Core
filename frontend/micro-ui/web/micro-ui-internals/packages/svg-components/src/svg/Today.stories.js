import React from "react";
import { Today } from "./Today";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Today",
  component: Today,
};

export const Default = () => <Today />;
export const Fill = () => <Today fill="blue" />;
export const Size = () => <Today height="50" width="50" />;
export const CustomStyle = () => <Today style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Today className="custom-class" />;
export const Clickable = () => <Today onClick={()=>console.log("clicked")} />;

const Template = (args) => <Today {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
