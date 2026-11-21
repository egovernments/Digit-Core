import React from "react";
import { QueryBuilder } from "./QueryBuilder";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "QueryBuilder",
  component: QueryBuilder,
};

export const Default = () => <QueryBuilder />;
export const Fill = () => <QueryBuilder fill="blue" />;
export const Size = () => <QueryBuilder height="50" width="50" />;
export const CustomStyle = () => <QueryBuilder style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <QueryBuilder className="custom-class" />;

export const Clickable = () => <QueryBuilder onClick={()=>console.log("clicked")} />;

const Template = (args) => <QueryBuilder {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
