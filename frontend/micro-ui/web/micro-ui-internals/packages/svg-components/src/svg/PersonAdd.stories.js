import React from "react";
import { PersonAdd } from "./PersonAdd";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PersonAdd",
  component: PersonAdd,
};

export const Default = () => <PersonAdd />;
export const Fill = () => <PersonAdd fill="blue" />;
export const Size = () => <PersonAdd height="50" width="50" />;
export const CustomStyle = () => <PersonAdd style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonAdd className="custom-class" />;

export const Clickable = () => <PersonAdd onClick={()=>console.log("clicked")} />;

const Template = (args) => <PersonAdd {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
